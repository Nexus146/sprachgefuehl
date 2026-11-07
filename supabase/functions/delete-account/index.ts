// Deletes only the authenticated caller's cloud data and auth account.
// Deploy with: supabase functions deploy delete-account
// The service-role key is provided by Supabase to the Edge Function runtime and never
// reaches the Android app.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

Deno.serve(async (request) => {
  if (request.method !== "POST") {
    return json({ error: "method not allowed" }, 405);
  }

  const authorization = request.headers.get("Authorization") ?? "";
  if (!authorization.startsWith("Bearer ")) {
    return json({ error: "missing session" }, 401);
  }

  const url = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !anonKey || !serviceKey) {
    return json({ error: "server is not configured" }, 500);
  }

  const userClient = createClient(url, anonKey, {
    global: { headers: { Authorization: authorization } },
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const { data, error } = await userClient.auth.getUser();
  if (error || !data.user) {
    return json({ error: "invalid session" }, 401);
  }

  const userId = data.user.id;

  try {
    const { data: files, error: listError } = await admin.storage
      .from("recordings")
      .list(userId, { limit: 1000 });
    if (listError) throw listError;

    const objectNames = (files ?? [])
      .map((file) => file.name)
      .filter((name) => name.length > 0)
      .map((name) => `${userId}/${name}`);
    if (objectNames.length > 0) {
      const { error: removeError } = await admin.storage
        .from("recordings")
        .remove(objectNames);
      if (removeError) throw removeError;
    }

    const { error: rowError } = await admin
      .from("learning_states")
      .delete()
      .eq("user_id", userId);
    if (rowError) throw rowError;

    const { error: deleteError } = await admin.auth.admin.deleteUser(userId);
    if (deleteError) throw deleteError;

    return json({ deleted: true });
  } catch (error) {
    console.error("delete-account failed", error);
    return json({ error: "could not delete account" }, 500);
  }
});
