// TODO(person-B): fill in during weeks 6-9 as the API/WebSocket take shape.

const API_BASE = "http://localhost:8080";
const WS_URL = "ws://localhost:8080/ws/orderbook";
const SYMBOL = "DEMO"; // TODO: make this selectable if multi-symbol is ever added

const statusEl = document.getElementById("connection-status");
const bidsBody = document.getElementById("bids-body");
const asksBody = document.getElementById("asks-body");
const tradeFeedList = document.getElementById("trade-feed-list");
const submitBtn = document.getElementById("submit-btn");

function connectWebSocket() {
  // TODO: open a WebSocket to WS_URL, update statusEl on open/close/error,
  // and call renderBook(...) / appendTrade(...) on incoming messages.
  throw new Error("TODO: implement connectWebSocket");
}

function renderBook(snapshot) {
  // TODO: snapshot shape TBD by OrderBookWebSocketHandler — likely
  // { bids: [{price, quantity}], asks: [{price, quantity}] }
  // Render best price at the top for both sides.
  throw new Error("TODO: implement renderBook");
}

function appendTrade(trade) {
  // TODO: prepend a <li> to tradeFeedList, cap the list length (e.g. 20)
  throw new Error("TODO: implement appendTrade");
}

async function submitOrder() {
  const side = document.getElementById("side").value;
  const type = document.getElementById("type").value;
  const price = document.getElementById("price").value;
  const quantity = document.getElementById("quantity").value;

  // TODO: POST to `${API_BASE}/api/orders` with { symbol: SYMBOL, side, type, price, quantity }
  // and surface errors to the user instead of just console.error.
  throw new Error("TODO: implement submitOrder");
}

submitBtn.addEventListener("click", submitOrder);
connectWebSocket();
