// Admin console backend. Only callers listed in public.admins may use it.
// The service-role key lives in the Edge Function runtime and never reaches the app.
// Deploy with: supabase functions deploy admin-users
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const json = (body, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

// Far in the future: the account cannot sign in again until an admin lifts the ban.
const BAN_FOREVER = "876000h";

const strings = (value) =>
  Array.isArray(value) ? value.filter((item) => typeof item === "string") : [];

const profileOf = (progress) => {
  const profile = progress?.profile ?? {};
  return {
    name: typeof profile.name === "string" ? profile.name : "",
    reason: typeof profile.reasonId === "string" ? profile.reasonId : "",
    interests: strings(profile.interests),
    dailyMinutes: Number(profile.dailyMinutes ?? 0) || 0,
  };
};

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "method not allowed" }, 405);

  const authorization = request.headers.get("Authorization") ?? "";
  if (!authorization.startsWith("Bearer ")) return json({ error: "missing session" }, 401);

  const url = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !anonKey || !serviceKey) return json({ error: "server is not configured" }, 500);

  const userClient = createClient(url, anonKey, {
    global: { headers: { Authorization: authorization } },
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const { data: caller, error: callerError } = await userClient.auth.getUser();
  if (callerError || !caller.user) return json({ error: "invalid session" }, 401);

  const { data: adminRow, error: adminError } = await admin
    .from("admins")
    .select("user_id")
    .eq("user_id", caller.user.id)
    .maybeSingle();
  if (adminError) return json({ error: "could not check admin rights" }, 500);
  if (!adminRow) return json({ error: "not an admin" }, 403);

  const body = await request.json().catch(() => ({}));
  const action = String(body.action ?? "list");

  const removeRecordings = async (userId) => {
    const { data: files } = await admin.storage.from("recordings").list(userId, { limit: 1000 });
    const names = (files ?? [])
      .map((file) => file.name)
      .filter((name) => name.length > 0)
      .map((name) => `${userId}/${name}`);
    if (names.length > 0) await admin.storage.from("recordings").remove(names);
    return names.length;
  };

  try {
    if (action === "list") {
      const { data: page, error } = await admin.auth.admin.listUsers({ page: 1, perPage: 200 });
      if (error) throw error;
      const { data: states, error: stateError } = await admin
        .from("learning_states")
        .select("user_id, revision, server_updated_at, progress");
      if (stateError) throw stateError;
      const byUser = new Map();
      for (const row of states ?? []) byUser.set(String(row.user_id), row);
      const users = (page?.users ?? []).map((user) => {
        const state = byUser.get(user.id);
        const progress = state?.progress ?? {};
        const profile = profileOf(progress);
        const lessons = Array.isArray(progress.completedLessons)
          ? progress.completedLessons.length
          : 0;
        const seconds = Number(progress.studySeconds ?? 0);
        return {
          id: user.id,
          email: user.email ?? "",
          confirmed: Boolean(user.email_confirmed_at),
          banned: Boolean(user.banned_until && new Date(user.banned_until) > new Date()),
          createdAt: user.created_at,
          lastSignInAt: user.last_sign_in_at ?? null,
          hasCloud: Boolean(state),
          revision: state ? Number(state.revision) : 0,
          updatedAt: state ? state.server_updated_at : null,
          lessons,
          minutes: Math.floor(seconds / 60),
          name: profile.name,
          reason: profile.reason,
          interests: profile.interests,
          dailyMinutes: profile.dailyMinutes,
          onboarded: progress.onboarded === true,
        };
      });
      return json({ users });
    }

    if (action === "detail") {
      const userId = String(body.userId ?? "");
      if (!userId) return json({ error: "userId fehlt" }, 400);
      const { data: state, error } = await admin
        .from("learning_states")
        .select("progress, revision, server_updated_at")
        .eq("user_id", userId)
        .maybeSingle();
      if (error) throw error;
      const progress = state?.progress ?? {};
      const profile = profileOf(progress);
      const lessons = Array.isArray(progress.completedLessons) ? progress.completedLessons : [];
      const seconds = Number(progress.studySeconds ?? 0);
      const { data: files } = await admin.storage.from("recordings").list(userId, { limit: 200 });
      const recordings = (files ?? []).filter((file) => file.name && file.id);
      const bytes = recordings.reduce(
        (sum, file) => sum + Number(file.metadata?.size ?? 0),
        0
      );
      return json({
        detail: {
          id: userId,
          hasCloud: Boolean(state),
          name: profile.name,
          reason: profile.reason,
          interests: profile.interests,
          dailyMinutes: profile.dailyMinutes,
          lessons: lessons.length,
          minutes: Math.floor(seconds / 60),
          onboarded: progress.onboarded === true,
          recordingCount: recordings.length,
          recordingBytes: bytes,
          revision: state ? Number(state.revision) : 0,
          updatedAt: state ? state.server_updated_at : null,
        },
      });
    }

    if (action === "create") {
      const email = String(body.email ?? "").trim().toLowerCase();
      const password = String(body.password ?? "");
      if (!email.includes("@") || email.length < 5) {
        return json({ error: "Bitte gib eine gültige E-Mail-Adresse ein." }, 400);
      }
      if (password.length < 8) {
        return json({ error: "Das Passwort braucht mindestens 8 Zeichen." }, 400);
      }
      const { data, error } = await admin.auth.admin.createUser({
        email,
        password,
        email_confirm: true,
      });
      if (error) {
        const message = error.message.includes("already")
          ? "Für diese Adresse gibt es schon ein Konto."
          : error.message;
        return json({ error: message }, 400);
      }
      return json({ created: { id: data.user?.id ?? "", email } });
    }

    if (action === "set-password") {
      const userId = String(body.userId ?? "");
      const password = String(body.password ?? "");
      if (!userId) return json({ error: "userId fehlt" }, 400);
      if (password.length < 8) {
        return json({ error: "Das Passwort braucht mindestens 8 Zeichen." }, 400);
      }
      const { error } = await admin.auth.admin.updateUserById(userId, { password });
      if (error) throw error;
      return json({ updated: true });
    }

    if (action === "set-email") {
      const userId = String(body.userId ?? "");
      const email = String(body.email ?? "").trim().toLowerCase();
      if (!userId) return json({ error: "userId fehlt" }, 400);
      if (!email.includes("@") || email.length < 5) {
        return json({ error: "Bitte gib eine gültige E-Mail-Adresse ein." }, 400);
      }
      const { error } = await admin.auth.admin.updateUserById(userId, {
        email,
        email_confirm: true,
      });
      if (error) {
        const message = error.message.includes("already")
          ? "Für diese Adresse gibt es schon ein Konto."
          : error.message;
        return json({ error: message }, 400);
      }
      return json({ updated: true });
    }

    if (action === "ban" || action === "unban") {
      const userId = String(body.userId ?? "");
      if (!userId) return json({ error: "userId fehlt" }, 400);
      if (userId === caller.user.id) {
        return json({ error: "Das eigene Admin-Konto lässt sich nicht sperren." }, 400);
      }
      const { error } = await admin.auth.admin.updateUserById(userId, {
        ban_duration: action === "ban" ? BAN_FOREVER : "none",
      });
      if (error) throw error;
      return json({ updated: true });
    }

    if (action === "reset-progress") {
      const userId = String(body.userId ?? "");
      if (!userId) return json({ error: "userId fehlt" }, 400);
      if (userId === caller.user.id) {
        return json({ error: "Das eigene Admin-Konto wird nicht zurückgesetzt." }, 400);
      }
      const removed = await removeRecordings(userId);
      const { error } = await admin.from("learning_states").delete().eq("user_id", userId);
      if (error) throw error;
      return json({ updated: true, recordings: removed });
    }

    if (action === "delete") {
      const userId = String(body.userId ?? "");
      if (!userId) return json({ error: "userId fehlt" }, 400);
      if (userId === caller.user.id) {
        return json({ error: "Das eigene Admin-Konto lässt sich hier nicht löschen." }, 400);
      }
      await removeRecordings(userId);
      await admin.from("learning_states").delete().eq("user_id", userId);
      const { error } = await admin.auth.admin.deleteUser(userId);
      if (error) throw error;
      return json({ deleted: true });
    }

    return json({ error: "unknown action" }, 400);
  } catch (error) {
    console.error("admin-users failed", error);
    return json({ error: "Die Aktion ist fehlgeschlagen." }, 500);
  }
});
