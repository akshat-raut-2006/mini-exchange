const API_BASE = "http://localhost:8080";
const WS_URL = "ws://localhost:8080/ws/orderbook";
const SYMBOL = "DEMO";

const statusEl = document.getElementById("connection-status");
const bidsBody = document.getElementById("bids-body");
const asksBody = document.getElementById("asks-body");
const tradeFeedList = document.getElementById("trade-feed-list");
const submitBtn = document.getElementById("submit-btn");

function connectWebSocket() {
  const ws = new WebSocket(WS_URL);

  ws.onopen = () => {
    statusEl.textContent = "connected";
  };
  ws.onclose = () => {
    statusEl.textContent = "disconnected — retrying in 3s";
    setTimeout(connectWebSocket, 3000);
  };
  ws.onerror = () => {
    statusEl.textContent = "connection error";
  };
  ws.onmessage = (event) => {
    const snapshot = JSON.parse(event.data);
    renderBook(snapshot);
  };
}

// snapshot shape: { symbol, bids: [{price, totalQuantity}], asks: [{price, totalQuantity}] }
function renderBook(snapshot) {
  bidsBody.innerHTML = snapshot.bids
    .map(level => `<tr><td>${Number(level.price).toFixed(2)}</td><td>${level.totalQuantity}</td></tr>`)
    .join("");

  asksBody.innerHTML = [...snapshot.asks]
    .reverse()
    .map(level => `<tr><td>${Number(level.price).toFixed(2)}</td><td>${level.totalQuantity}</td></tr>`)
    .join("");
}

function appendTrade(trade) {
  const li = document.createElement("li");
  const time = new Date(trade.executedAt).toLocaleTimeString();
  li.textContent = `${Number(trade.price).toFixed(2)} x ${trade.quantity} @ ${time}`;
  tradeFeedList.prepend(li);

  while (tradeFeedList.children.length > 20) {
    tradeFeedList.removeChild(tradeFeedList.lastChild);
  }
}

async function submitOrder() {
  const side = document.getElementById("side").value;
  const type = document.getElementById("type").value;
  const priceInput = document.getElementById("price").value;
  const quantity = document.getElementById("quantity").value;

  const body = {
    symbol: SYMBOL,
    side,
    type,
    price: type === "MARKET" ? null : Number(priceInput),
    quantity: Number(quantity)
  };

  try {
    const res = await fetch(`${API_BASE}/api/orders`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      throw new Error(`Server responded with ${res.status}`);
    }

    const result = await res.json();
    result.trades?.forEach(appendTrade);
  } catch (err) {
    alert(`Failed to submit order: ${err.message}`);
  }
}

const typeSelect = document.getElementById("type");
const priceInput = document.getElementById("price");
typeSelect.addEventListener("change", () => {
  const isMarket = typeSelect.value === "MARKET";
  priceInput.disabled = isMarket;
  priceInput.value = isMarket ? "" : priceInput.value;
});

submitBtn.addEventListener("click", submitOrder);
connectWebSocket();