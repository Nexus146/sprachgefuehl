-- Sprachgefühl cloud accounts
-- Run this file in the Supabase SQL editor (or `supabase db push`).
-- The app talks to PostgREST and Storage; it never gets a service-role key.

create table if not exists public.learning_states (
    user_id uuid primary key references auth.users (id) on delete cascade,
    progress jsonb not null default '{}'::jsonb
        check (jsonb_typeof(progress) = 'object'),
    schema_version integer not null default 1,
    revision bigint not null default 1 check (revision > 0),
    client_updated_at timestamptz,
    server_updated_at timestamptz not null default now()
);

comment on table public.learning_states is
    'One flexible progress document per learner. The app owns the JSON schema.';
comment on column public.learning_states.revision is
    'Optimistic concurrency token: a stale client gets a conflict instead of overwriting.';

alter table public.learning_states enable row level security;

-- Reads may go through the Data API; writes only through push_learning_state below.
revoke all on table public.learning_states from anon, authenticated;
grant select on table public.learning_states to authenticated;

create or replace function public.is_verified_user()
returns boolean
language sql
stable
security definer
set search_path = public, auth
as $$
    select exists (
        select 1
        from auth.users u
        where u.id = auth.uid()
          and u.email_confirmed_at is not null
    );
$$;

revoke all on function public.is_verified_user() from public, anon;
grant execute on function public.is_verified_user() to authenticated;

drop policy if exists "read own learning state" on public.learning_states;
create policy "read own learning state"
    on public.learning_states
    for select
    to authenticated
    using (auth.uid() = user_id and public.is_verified_user());

create or replace function public.push_learning_state(
    p_progress jsonb,
    p_expected_revision bigint,
    p_client_updated_at timestamptz default now(),
    p_force boolean default false
)
returns table (
    revision bigint,
    conflict boolean,
    progress jsonb,
    server_updated_at timestamptz
)
language plpgsql
security definer
set search_path = public, auth
as $$
declare
    v_uid uuid := auth.uid();
    v_current public.learning_states%rowtype;
    v_revision bigint;
    v_server_updated_at timestamptz;
begin
    if v_uid is null or not public.is_verified_user() then
        raise exception 'not an authenticated verified user' using errcode = '42501';
    end if;

    if p_progress is null or jsonb_typeof(p_progress) <> 'object' then
        raise exception 'progress must be a JSON object' using errcode = '22023';
    end if;

    select *
    into v_current
    from public.learning_states
    where user_id = v_uid
    for update;

    if not found then
        insert into public.learning_states (
            user_id, progress, schema_version, revision, client_updated_at, server_updated_at
        )
        values (
            v_uid, p_progress, 1, 1, coalesce(p_client_updated_at, now()), now()
        )
        returning revision, server_updated_at
        into v_revision, v_server_updated_at;

        return query select v_revision, false, p_progress, v_server_updated_at;
        return;
    end if;

    if p_force or v_current.revision = p_expected_revision then
        update public.learning_states
        set progress = p_progress,
            revision = v_current.revision + 1,
            client_updated_at = coalesce(p_client_updated_at, now()),
            server_updated_at = now()
        where user_id = v_uid
        returning revision, server_updated_at
        into v_revision, v_server_updated_at;

        return query select v_revision, false, p_progress, v_server_updated_at;
    else
        return query
            select v_current.revision, true, v_current.progress, v_current.server_updated_at;
    end if;
end;
$$;

revoke all on function public.push_learning_state(jsonb, bigint, timestamptz, boolean)
    from public, anon;
grant execute on function public.push_learning_state(jsonb, bigint, timestamptz, boolean)
    to authenticated;

-- Private recordings. Object names are {auth.uid()}/{recording-id}.m4a.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'recordings',
    'recordings',
    false,
    12582912,
    array['audio/mp4', 'audio/m4a', 'audio/x-m4a', 'audio/aac']
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "read own recordings" on storage.objects;
create policy "read own recordings"
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'recordings'
        and (storage.foldername(name))[1] = auth.uid()::text
        and public.is_verified_user()
    );

drop policy if exists "insert own recordings" on storage.objects;
create policy "insert own recordings"
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'recordings'
        and (storage.foldername(name))[1] = auth.uid()::text
        and public.is_verified_user()
    );

drop policy if exists "update own recordings" on storage.objects;
create policy "update own recordings"
    on storage.objects
    for update
    to authenticated
    using (
        bucket_id = 'recordings'
        and (storage.foldername(name))[1] = auth.uid()::text
        and public.is_verified_user()
    )
    with check (
        bucket_id = 'recordings'
        and (storage.foldername(name))[1] = auth.uid()::text
        and public.is_verified_user()
    );

drop policy if exists "delete own recordings" on storage.objects;
create policy "delete own recordings"
    on storage.objects
    for delete
    to authenticated
    using (
        bucket_id = 'recordings'
        and (storage.foldername(name))[1] = auth.uid()::text
        and public.is_verified_user()
    );
