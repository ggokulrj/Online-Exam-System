"use strict";
/* ---------- helpers ---------- */
const $ = (s) => document.querySelector(s);
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
let timer = null, toastTimer = null, submitting = false;

function toast(msg, type = "") {
  const t = $("#toast");
  t.textContent = msg; t.className = type; t.style.display = "block";
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (t.style.display = "none"), 4000);
}
const getUser = () => {
  const token = localStorage.getItem("token");
  return token ? { token, username: localStorage.getItem("username"), role: localStorage.getItem("role") } : null;
};
function logout() {
  ["token", "username", "role", "activeExam"].forEach((k) => localStorage.removeItem(k));
  clearInterval(timer);
  route();
}
const getActive = () => JSON.parse(localStorage.getItem("activeExam") || "null");
const setActive = (a) => (a ? localStorage.setItem("activeExam", JSON.stringify(a)) : localStorage.removeItem("activeExam"));

/* One function for every backend call. Adds the JWT token automatically. */
async function api(path, method = "GET", body) {
  const headers = { "Content-Type": "application/json" };
  const user = getUser();
  if (user) headers.Authorization = "Bearer " + user.token;
  const res = await fetch(path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  let data = null;
  try { data = await res.json(); } catch (e) { /* empty body */ }
  if (res.status === 401 && user) { logout(); throw new Error("Session expired. Log in again."); }
  if (!res.ok) throw new Error((data && data.error) || "Request failed (" + res.status + ")");
  return data;
}

/* ---------- router ---------- */
function route() {
  const user = getUser();
  $("#nav").innerHTML = `<strong>Online Exam System</strong>` +
      (user ? `<div><span>${esc(user.username)} (${esc(user.role)})</span><button class="btn small" id="logout">Log out</button></div>` : "");
  if (user) $("#logout").onclick = logout;
  if (!user) renderAuth("login");
  else if (user.role === "ADMIN") renderAdmin();
  else if (getActive()) renderExam(getActive());
  else renderStudent();
}

/* ---------- login / register ---------- */
function renderAuth(mode) {
  const login = mode === "login";
  $("#app").innerHTML = `<div class="card auth">
    <div class="tabs"><button class="btn ${login ? "active" : ""}" data-m="login">Log in</button>
    <button class="btn ${login ? "" : "active"}" data-m="register">Register</button></div>
    <form id="f">
      <label>Username<input name="username" required minlength="3" maxlength="30" autocomplete="username"></label>
      <label>Password<input type="password" name="password" required ${login ? "" : 'minlength="6"'} maxlength="72" autocomplete="${login ? "current-password" : "new-password"}"></label>
      <button class="btn primary" type="submit">${login ? "Log in" : "Create account"}</button>
    </form></div>`;
  document.querySelectorAll(".tabs .btn").forEach((b) => (b.onclick = () => renderAuth(b.dataset.m)));
  $("#f").onsubmit = async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    try {
      if (login) {
        const r = await api("/api/auth/login", "POST", data);
        localStorage.setItem("token", r.token); localStorage.setItem("username", r.username); localStorage.setItem("role", r.role);
        route();
      } else {
        await api("/api/auth/register", "POST", data);
        toast("Account created. Log in now.", "ok"); renderAuth("login");
      }
    } catch (err) { toast(err.message, "error"); }
  };
}

/* ---------- student: dashboard ---------- */
async function renderStudent() {
  $("#app").innerHTML = `<div class="grid">
    <section class="card"><h2>Available exams</h2><div id="exams">Loading...</div></section>
    <section class="card"><h2>My performance</h2><div id="report">Loading...</div></section></div>`;
  try {
    const [exams, rep] = await Promise.all([api("/api/exams"), api("/api/attempts/my")]);
    $("#exams").innerHTML = exams.length ? exams.map((x) => `<div class="item"><div><b>${esc(x.title)}</b><div class="muted">${esc(x.description)}<br>${x.durationMinutes} min, pass at ${x.passPercentage}%</div></div>
      <button class="btn primary small" data-id="${x.id}">Start</button></div>`).join("") : `<p class="muted">No exams yet. Ask your admin to create one.</p>`;
    $("#exams").onclick = (e) => { const id = e.target.dataset.id; if (id) startExam(exams.find((x) => x.id == id)); };
    $("#report").innerHTML = `<div class="stats"><div><b>${rep.totalAttempts}</b>exams taken</div><div><b>${rep.averagePercentage}%</b>average</div><div><b>${rep.passedCount}</b>passed</div></div>` +
        (rep.attempts.length ? `<table><tr><th>Exam</th><th>Score</th><th>Result</th></tr>${rep.attempts.map((a) =>
            `<tr><td>${esc(a.examTitle)}</td><td>${a.score}/${a.totalQuestions} (${a.percentage}%)</td><td class="${a.passed ? "pass" : "fail"}">${a.passed ? "Pass" : "Fail"}</td></tr>`).join("")}</table>` : `<p class="muted">No results yet.</p>`);
  } catch (err) { toast(err.message, "error"); }
}

async function startExam(exam) {
  if (!confirm(`Start "${exam.title}"? You get ${exam.durationMinutes} minutes and only one attempt.`)) return;
  try {
    const r = await api(`/api/exams/${exam.id}/start`, "POST");
    // The countdown uses the browser clock; the server enforces the real deadline.
    setActive({ attemptId: r.attemptId, title: r.examTitle, questions: r.questions, answers: {}, endMs: Date.now() + exam.durationMinutes * 60000 });
    route();
  } catch (err) { toast(err.message, "error"); }
}

/* ---------- student: taking the exam ---------- */
function renderExam(a) {
  submitting = false;
  $("#app").innerHTML = `<div class="card"><div class="timer" id="timer"></div><h2>${esc(a.title)}</h2>
    <form id="exam">${a.questions.map((q, i) => `<div class="q"><b>${i + 1}. ${esc(q.text)}</b>
      ${["A", "B", "C", "D"].map((o) => `<label><input type="radio" name="${q.id}" value="${o}" ${a.answers[q.id] === o ? "checked" : ""}>${o}. ${esc(q["option" + o])}</label>`).join("")}</div>`).join("")}
      <button class="btn primary" type="submit">Submit exam</button></form></div>`;
  $("#exam").onchange = (e) => { a.answers[e.target.name] = e.target.value; setActive(a); };
  $("#exam").onsubmit = (e) => {
    e.preventDefault();
    const left = a.questions.length - Object.keys(a.answers).length;
    if (left > 0 && !confirm(`${left} question(s) unanswered. Submit anyway?`)) return;
    submitExam(a);
  };
  clearInterval(timer);
  const tick = () => {
    const ms = a.endMs - Date.now();
    if (ms <= 0) { clearInterval(timer); toast("Time is up. Submitting...", "error"); submitExam(a); return; }
    const s = Math.floor(ms / 1000);
    $("#timer").textContent = "Time left: " + String(Math.floor(s / 60)).padStart(2, "0") + ":" + String(s % 60).padStart(2, "0");
  };
  tick(); timer = setInterval(tick, 1000);
}

async function submitExam(a) {
  if (submitting) return;
  submitting = true; clearInterval(timer);
  try {
    const r = await api(`/api/attempts/${a.attemptId}/submit`, "POST", { answers: a.answers });
    setActive(null);
    $("#app").innerHTML = `<div class="card" style="text-align:center"><h2>${esc(r.examTitle)}</h2>
      <div class="big">${r.percentage}%</div><p>You scored ${r.score} out of ${r.totalQuestions}.</p>
      <p class="${r.passed ? "pass" : "fail"}">${r.passed ? "You passed" : "You did not pass"}</p>
      <button class="btn primary" id="back">Back to dashboard</button></div>`;
    $("#back").onclick = route;
  } catch (err) {
    submitting = false;
    toast(err.message, "error");
    if (/already submitted/.test(err.message)) { setActive(null); route(); }
  }
}

/* ---------- admin ---------- */
async function renderAdmin() {
  $("#app").innerHTML = `<div class="grid">
    <section class="card"><h2>Create exam</h2><form id="ef">
      <label>Title<input name="title" required></label>
      <label>Description<input name="description"></label>
      <label>Duration (minutes)<input type="number" name="durationMinutes" min="1" value="30" required></label>
      <label>Pass mark (%)<input type="number" name="passPercentage" min="1" max="100" value="40" required></label>
      <button class="btn primary" type="submit">Create exam</button></form></section>
    <section class="card"><h2>Exams</h2><div id="list">Loading...</div></section></div><div id="manage"></div>`;
  $("#ef").onsubmit = async (e) => {
    e.preventDefault();
    const d = Object.fromEntries(new FormData(e.target));
    d.durationMinutes = +d.durationMinutes; d.passPercentage = +d.passPercentage;
    try { await api("/api/admin/exams", "POST", d); toast("Exam created.", "ok"); e.target.reset(); renderAdmin(); }
    catch (err) { toast(err.message, "error"); }
  };
  try {
    const exams = await api("/api/exams");
    $("#list").innerHTML = exams.length ? exams.map((x) => `<div class="item"><div><b>${esc(x.title)}</b><div class="muted">ID ${x.id}, ${x.durationMinutes} min, pass at ${x.passPercentage}%</div></div>
      <div><button class="btn small" data-a="q" data-id="${x.id}">Add questions</button> <button class="btn small" data-a="r" data-id="${x.id}">Report</button></div></div>`).join("") : `<p class="muted">No exams yet. Create the first one.</p>`;
    $("#list").onclick = (e) => {
      const { a, id } = e.target.dataset; if (!a) return;
      const exam = exams.find((x) => x.id == id);
      a === "q" ? manageQuestions(exam) : showReport(exam);
    };
  } catch (err) { toast(err.message, "error"); }
}

function manageQuestions(exam) {
  $("#manage").innerHTML = `<section class="card"><h2>Add questions to "${esc(exam.title)}"</h2><form id="qf">
    <label>Question<textarea name="text" required rows="2"></textarea></label>
    <div class="grid"><label>Option A<input name="optionA" required></label><label>Option B<input name="optionB" required></label>
    <label>Option C<input name="optionC" required></label><label>Option D<input name="optionD" required></label></div>
    <label>Correct answer<select name="correctOption"><option>A</option><option>B</option><option>C</option><option>D</option></select></label>
    <button class="btn primary" type="submit">Add question</button></form><div id="added"></div></section>`;
  $("#manage").scrollIntoView({ behavior: "smooth" });
  $("#qf").onsubmit = async (e) => {
    e.preventDefault();
    const d = Object.fromEntries(new FormData(e.target));
    try {
      await api(`/api/admin/exams/${exam.id}/questions`, "POST", d);
      $("#added").insertAdjacentHTML("beforeend", `<p class="muted">Added: ${esc(d.text)} (answer ${d.correctOption})</p>`);
      e.target.reset(); toast("Question added.", "ok");
    } catch (err) { toast(err.message, "error"); }
  };
}

async function showReport(exam) {
  try {
    const r = await api(`/api/admin/exams/${exam.id}/report`);
    $("#manage").innerHTML = `<section class="card"><h2>Report: ${esc(r.title)}</h2><div class="stats">
      <div><b>${r.totalAttempts}</b>attempts</div><div><b>${r.averagePercentage}%</b>average</div><div><b>${r.highestPercentage}%</b>highest</div>
      <div><b>${r.lowestPercentage}%</b>lowest</div><div><b class="pass">${r.passCount}</b>passed</div><div><b class="fail">${r.failCount}</b>failed</div></div></section>`;
    $("#manage").scrollIntoView({ behavior: "smooth" });
  } catch (err) { toast(err.message, "error"); }
}

route();