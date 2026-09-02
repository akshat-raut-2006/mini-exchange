const API_BASE = "http://localhost:8080";
const WS_URL = "ws://localhost:8080/ws/orderbook";
const SYMBOLS = ["RELIANCE", "TCS", "INFY", "AAPL", "TSLA"];

// Safe to keep in frontend code — Supabase's anon key is designed to be
// public; access is governed by Supabase's own project-level rules, not
// by keeping this string secret. The JWT secret used to *verify* tokens
// server-side is the one that stays private, in api-server/.env.
const SUPABASE_URL = "https://vxibkcerkykulnmedvcu.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ4aWJrY2Vya3lrdWxubWVkdmN1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU1Nzg0NzQsImV4cCI6MjEwMTE1NDQ3NH0.f-BDPFBHrDGkRpYoZv_83CeloQdUBXlG0L78O_vIMv8";

const supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

const els = {
  authOverlay: document.getElementById("auth-overlay"),
  authTabs: document.querySelectorAll(".auth-tab"),
  authEmail: document.getElementById("auth-email"),
  authPassword: document.getElementById("auth-password"),
  togglePasswordBtn: document.getElementById("toggle-password-btn"),
  authSubmitBtn: document.getElementById("auth-submit-btn"),
  authError: document.getElementById("auth-error"),
  authNote: document.getElementById("auth-note"),

  terminalRoot: document.getElementById("terminal-root"),
  userEmail: document.getElementById("user-email"),
  signOutBtn: document.getElementById("sign-out-btn"),

  symbolSelector: document.getElementById("symbol-selector"),
  statusDot: document.getElementById("status-dot"),
  statusText: document.getElementById("connection-status"),
  lastPrice: document.getElementById("last-price"),
  lastPriceDelta: document.getElementById("last-price-delta"),
  spreadValue: document.getElementById("spread-value"),
  midValue: document.getElementById("mid-value"),
  spreadRowValue: document.getElementById("spread-row-value"),
  ladderAsks: document.getElementById("ladder-asks"),
  ladderBids: document.getElementById("ladder-bids"),
  tape: document.getElementById("trade-tape"),
  blotter: document.getElementById("my-orders-body"),
  sideToggle: document.getElementById("side-toggle"),
  typeToggle: document.getElementById("type-toggle"),
  priceInput: document.getElementById("price"),
  qtyInput: document.getElementById("quantity"),
  submitBtn: document.getElementById("submit-btn"),
  ticketError: document.getElementById("ticket-error"),
};

let state = {
  side: "BUY",
  type: "LIMIT",
  currentSymbol: SYMBOLS[0],
  lastPrice: null,
  bestBid: null,
  bestAsk: null,
  authMode: "signin", // or "signup"
  appStarted: false,  // guards against wiring up the terminal more than once
};

// ---------- Auth ----------

els.authTabs.forEach(tab => {
  tab.addEventListener("click", () => {
    state.authMode = tab.dataset.mode;
    els.authTabs.forEach(t => t.classList.toggle("active", t === tab));
    els.authSubmitBtn.textContent = state.authMode === "signup" ? "Create Account" : "Sign In";
    els.authError.textContent = "";
    els.authNote.textContent = "";
  });
});

els.togglePasswordBtn.addEventListener("click", () => {
  const isHidden = els.authPassword.type === "password";
  els.authPassword.type = isHidden ? "text" : "password";
  els.togglePasswordBtn.textContent = isHidden ? "Hide" : "Show";
});

els.authSubmitBtn.addEventListener("click", async () => {
  els.authError.textContent = "";
  els.authNote.textContent = "";

  const email = els.authEmail.value.trim();
  const password = els.authPassword.value;

  if (!email || !password) {
    els.authError.textContent = "Enter both an email and a password.";
    return;
  }

  const { data, error } = state.authMode === "signup"
    ? await supabaseClient.auth.signUp({ email, password })
    : await supabaseClient.auth.signInWithPassword({ email, password });

  if (error) {
    // Supabase's wording varies by version ("User already registered",
    // "already exists", etc.) - match loosely rather than one exact string.
    const msg = error.message?.toLowerCase() ?? "";
    els.authError.textContent = msg.includes("already")
      ? "An account with this email already exists — try signing in instead."
      : error.message;
    return;
  }

  // When "Confirm email" is enabled in Supabase, signing up with an email
  // that's already registered returns no error at all (this is intentional
  // on Supabase's part, to prevent attackers from probing which emails are
  // registered) - instead you get a user object with an empty `identities`
  // array. That's the only way to detect it in this configuration.
  if (state.authMode === "signup" && data?.user?.identities?.length === 0) {
    els.authError.textContent = "An account with this email already exists — try signing in instead.";
    return;
  }

  if (state.authMode === "signup") {
    els.authNote.textContent = "Account created — check your email if confirmation is required, then sign in.";
  }
  // On success, onAuthStateChange (below) handles showing the terminal.
});

els.signOutBtn.addEventListener("click", async () => {
  await supabaseClient.auth.signOut();
});

supabaseClient.auth.onAuthStateChange((_event, session) => {
  if (session) {
    showTerminal(session);
  } else {
    showAuthOverlay();
  }
});

function showAuthOverlay() {
  els.authOverlay.classList.remove("hidden");
  els.terminalRoot.classList.add("hidden");

  // Reset the form so a previous session's email/password don't linger for
  // the next person signing in on this browser.
  els.authEmail.value = "";
  els.authPassword.value = "";
  els.authPassword.type = "password";
  els.togglePasswordBtn.textContent = "Show";
  els.authError.textContent = "";
  els.authNote.textContent = "";
  state.authMode = "signin";
  els.authTabs.forEach(t => t.classList.toggle("active", t.dataset.mode === "signin"));
  els.authSubmitBtn.textContent = "Sign In";
}

function showTerminal(session) {
  els.authOverlay.classList.add("hidden");
  els.terminalRoot.classList.remove("hidden");
  els.userEmail.textContent = session.user.email;

  if (!state.appStarted) {
    state.appStarted = true;
    startApp();
  }
}

/** Current access token, or null if somehow not signed in — used as the bearer token on order requests. */
async function getAccessToken() {
  const { data: { session } } = await supabaseClient.auth.getSession();
  return session?.access_token ?? null;
}

// ---------- App startup (only runs once, after first sign-in) ----------

function startApp() {
  renderSymbolSelector();
  switchSymbol(state.currentSymbol);
  connectWebSocket();
}

// ---------- Symbol selector ----------

function renderSymbolSelector() {
  els.symbolSelector.innerHTML = SYMBOLS.map(sym => `
    <button type="button" class="symbol-btn ${sym === state.currentSymbol ? "active" : ""}" data-symbol="${sym}">
      ${sym}
    </button>`).join("");
}

els.symbolSelector.addEventListener("click", (e) => {
  const btn = e.target.closest(".symbol-btn");
  if (!btn || btn.dataset.symbol === state.currentSymbol) return;
  switchSymbol(btn.dataset.symbol);
});

async function switchSymbol(symbol) {
  state.currentSymbol = symbol;
  state.lastPrice = null;
  state.bestBid = null;
  state.bestAsk = null;

  document.getElementById("symbol-label").textContent = symbol;
  renderSymbolSelector();

  // Reset per-symbol displays — the old symbol's numbers shouldn't linger.
  els.lastPrice.textContent = "—";
  els.lastPrice.style.color = "var(--text-primary)";
  els.lastPriceDelta.textContent = "";
  updateSpreadAndMid();
  els.tape.innerHTML = `<div class="empty-state">No trades yet — printed fills will appear here.</div>`;

  // WebSocket only pushes on the next trade, so pull the current book directly
  // via REST the moment we switch, instead of waiting for something to happen.
  try {
    const res = await fetch(`${API_BASE}/api/orders/book?symbol=${encodeURIComponent(symbol)}`);
    if (res.ok) renderBook(await res.json());
  } catch (err) {
    console.error("Failed to load book for", symbol, err);
  }
}

// ---------- WebSocket / book rendering ----------

function connectWebSocket() {
  const ws = new WebSocket(WS_URL);

  ws.onopen = () => {
    els.statusDot.className = "status-dot connected";
    els.statusText.textContent = "connected";
  };
  ws.onclose = () => {
    els.statusDot.className = "status-dot disconnected";
    els.statusText.textContent = "disconnected — retrying";
    setTimeout(connectWebSocket, 3000);
  };
  ws.onerror = () => {
    els.statusText.textContent = "connection error";
  };
  ws.onmessage = (event) => {
    const snapshot = JSON.parse(event.data);
    // The server broadcasts every symbol's updates to every connected client;
    // only render the one the user is actually looking at right now.
    if (snapshot.symbol === state.currentSymbol) {
      renderBook(snapshot);
    }
  };
}

// snapshot: { symbol, bids: [{price, totalQuantity}], asks: [{price, totalQuantity}] }
function renderBook(snapshot) {
  const bids = snapshot.bids || [];
  const asks = snapshot.asks || [];

  state.bestBid = bids.length ? Number(bids[0].price) : null;
  state.bestAsk = asks.length ? Number(asks[0].price) : null;

  const maxQty = Math.max(
    1,
    ...bids.map(l => Number(l.totalQuantity)),
    ...asks.map(l => Number(l.totalQuantity))
  );

  // Worst ask at top, best ask just above the spread — mirrors a real depth ladder.
  els.ladderAsks.innerHTML = [...asks].reverse().map(l => ladderRow(l, "ask", maxQty)).join("")
    || `<div class="empty-state">No asks resting.</div>`;

  els.ladderBids.innerHTML = bids.map(l => ladderRow(l, "bid", maxQty)).join("")
    || `<div class="empty-state">No bids resting.</div>`;

  updateSpreadAndMid();
}

function ladderRow(level, side, maxQty) {
  const price = Number(level.price);
  const qty = Number(level.totalQuantity);
  const pct = Math.max(4, (qty / maxQty) * 100);
  return `
    <div class="ladder-row ${side}">
      <span class="price">${price.toFixed(2)}</span>
      <span class="qty">${qty}</span>
      <div class="depth-bar-track"><div class="depth-bar" style="width:${pct}%"></div></div>
    </div>`;
}

function updateSpreadAndMid() {
  if (state.bestBid != null && state.bestAsk != null) {
    const spread = state.bestAsk - state.bestBid;
    const mid = (state.bestAsk + state.bestBid) / 2;
    els.spreadValue.textContent = spread.toFixed(2);
    els.midValue.textContent = mid.toFixed(2);
    els.spreadRowValue.textContent = `${spread.toFixed(2)} (mid ${mid.toFixed(2)})`;
  } else {
    els.spreadValue.textContent = "—";
    els.midValue.textContent = "—";
    els.spreadRowValue.textContent = "—";
  }
}

// ---------- Trade tape ----------

// trade: { tradeId, symbol, price, quantity, executedAt }
function appendTrade(trade) {
  els.tape.querySelector(".empty-state")?.remove();

  const price = Number(trade.price);
  const direction = state.lastPrice == null ? null : price >= state.lastPrice ? "up" : "down";
  state.lastPrice = price;
  updateLastPrice(price, direction);

  const row = document.createElement("div");
  row.className = `tape-row ${direction === "down" ? "flash-down" : "flash-up"}`;
  const time = new Date(trade.executedAt).toLocaleTimeString();
  row.innerHTML = `
    <span>${trade.symbol}</span>
    <span class="price ${direction === "down" ? "sell" : "buy"}">${price.toFixed(2)}</span>
    <span>${trade.quantity}</span>
    <span>${time}</span>`;
  els.tape.prepend(row);

  while (els.tape.children.length > 30) {
    els.tape.removeChild(els.tape.lastChild);
  }
}

function updateLastPrice(price, direction) {
  els.lastPrice.textContent = price.toFixed(2);
  els.lastPrice.style.color = direction === "down" ? "var(--red)" : direction === "up" ? "var(--green)" : "var(--text-primary)";
  if (direction) {
    els.lastPriceDelta.className = `last-price-delta ${direction}`;
    els.lastPriceDelta.textContent = direction === "up" ? "▲" : "▼";
  }
}

// ---------- My orders (blotter) ----------

function appendMyOrder({ orderId, symbol, side, type, price, quantity, status }) {
  els.blotter.querySelector(".empty-state")?.remove();

  const row = document.createElement("div");
  row.className = "blotter-row";
  const shortId = orderId.slice(0, 8);
  const time = new Date().toLocaleTimeString();
  const badgeClass = status.toLowerCase();
  row.innerHTML = `
    <span title="${orderId}">${shortId}…</span>
    <span>${symbol}</span>
    <span class="side ${side.toLowerCase()}">${side}</span>
    <span>${type}</span>
    <span>${price != null ? Number(price).toFixed(2) : "MKT"}</span>
    <span>${quantity}</span>
    <span><span class="badge ${badgeClass}">${status}</span></span>
    <span>${time}</span>`;
  els.blotter.prepend(row);

  while (els.blotter.children.length > 30) {
    els.blotter.removeChild(els.blotter.lastChild);
  }
}

// ---------- Order ticket controls ----------

els.sideToggle.addEventListener("click", (e) => {
  const btn = e.target.closest(".side-btn");
  if (!btn) return;
  state.side = btn.dataset.side;
  els.sideToggle.querySelectorAll(".side-btn").forEach(b => b.classList.toggle("active", b === btn));
  els.submitBtn.className = `submit-btn ${state.side === "BUY" ? "buy" : "sell"}`;
  els.submitBtn.textContent = `Place ${state.side === "BUY" ? "Buy" : "Sell"} Order`;
});

els.typeToggle.addEventListener("click", (e) => {
  const btn = e.target.closest(".type-btn");
  if (!btn) return;
  state.type = btn.dataset.type;
  els.typeToggle.querySelectorAll(".type-btn").forEach(b => b.classList.toggle("active", b === btn));
  const isMarket = state.type === "MARKET";
  els.priceInput.disabled = isMarket;
  if (isMarket) els.priceInput.value = "";
});

async function submitOrder() {
  els.ticketError.textContent = "";

  const quantity = Number(els.qtyInput.value);
  const price = state.type === "MARKET" ? null : Number(els.priceInput.value);

  if (!quantity || quantity <= 0) {
    els.ticketError.textContent = "Enter a quantity greater than 0.";
    return;
  }
  if (state.type === "LIMIT" && (!price || price <= 0)) {
    els.ticketError.textContent = "Enter a price greater than 0.";
    return;
  }

  const token = await getAccessToken();
  if (!token) {
    els.ticketError.textContent = "Your session expired — please sign in again.";
    await supabaseClient.auth.signOut();
    return;
  }

  const body = { symbol: state.currentSymbol, side: state.side, type: state.type, price, quantity };

  try {
    const res = await fetch(`${API_BASE}/api/orders`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`Server responded with ${res.status}`);

    const result = await res.json();
    appendMyOrder({
      orderId: result.orderId,
      symbol: state.currentSymbol,
      side: state.side,
      type: state.type,
      price,
      quantity,
      status: result.status,
    });
    (result.trades || []).forEach(appendTrade);
  } catch (err) {
    els.ticketError.textContent = `Failed to submit order: ${err.message}`;
  }
}

els.submitBtn.addEventListener("click", submitOrder);