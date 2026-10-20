# Mega Mart Fullstack O2

Mega Mart is a Java 17 and Spring Boot shop with a PostgreSQL database. The same Java application serves the customer shop, the seller dashboard, and JSON REST endpoints. It does not need a Node.js server.

## Features

- Customer storefront, product catalog, cart, and checkout with customer name, phone, and typed delivery address.
- Cash on Delivery orders only. No payment gateway or online payment code is included.
- Separate seller sign-in and dashboard for customer orders, phone/address details, order status, cash collection, and product/stock management.
- Orders and products persist in PostgreSQL. Stock is locked and checked during checkout to prevent overselling.
- New-order alert on the seller dashboard; the page refreshes every 15 seconds while open. Orders remain saved if the seller is away.
- Animated welcome screen with the butterfly, glow, orbit and floating effects.
- REST API, request validation, pagination, database migrations, and Railway Docker deployment configuration.

There is no OTP/SMS, maps/geolocation, payment provider, or outbound third-party service integration. Checkout accepts the recipient details directly. Customer OTP login is not included because it requires an OTP provider.

## Project structure

```text
src/main/java/com/megamart/
  api/controller/       REST request handlers
  api/dto/               REST request/response models and API errors
  controller/            Server-rendered shop and seller page controllers
  service/               Business rules and transactions
  repository/            Spring Data persistence access
  model/                 PostgreSQL entities and enums
  config/                Security, database URL parsing, and CORS
  exception/             Business and page error handling
src/main/resources/
  db/migration/          Flyway SQL migrations V1–V5
  static/                CSS, JavaScript, and favicon
  templates/             Shop, checkout, seller login/orders/products pages
```

Controllers call the service layer; services call repositories. Controllers do not access repositories directly.

## Run locally

Install Java 17+, Maven 3.9+, and PostgreSQL. Create a PostgreSQL database (for example, `megamart`), then set these PowerShell variables with your local values:

```powershell
$env:DATABASE_URL = 'postgresql://USER:PASSWORD@localhost:5432/megamart'
$env:SELLER_USERNAME = 'seller'
$env:SELLER_PASSWORD = 'choose-a-unique-password-at-least-12-characters'
mvn spring-boot:run
```

Open `http://localhost:8080/`. The root route opens the shop; use **Seller sign in** to open the seller login. Flyway creates and updates the database schema on startup. Do not commit real passwords or database credentials.

The seller account is configured through `SELLER_USERNAME` and `SELLER_PASSWORD`. The password must be at least 12 characters. Legacy `ADMIN_USERNAME` and `ADMIN_PASSWORD` variables are also accepted. Seller page login uses a secure Spring Security session; protected JSON seller endpoints use HTTP Basic with the same configured credentials.

## REST endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/health` | Public | Health status for Railway |
| `GET` | `/api/products?page=0&size=24` | Public | Active product catalog; optional `category` filter |
| `GET` | `/api/products/categories` | Public | Active categories |
| `GET` | `/api/products/{id}` | Public | One active product |
| `GET` | `/api/products/{id}/image` | Public | Product image uploaded by the seller |
| `GET` | `/api/products/sku/{sku}` | Public | Active product by SKU |
| `POST` | `/api/orders` | Public | Place a COD order |
| `GET` | `/api/admin/products?page=0&size=50` | Seller | List all products |
| `GET` | `/api/admin/products/{id}` | Seller | Read a product |
| `POST` | `/api/admin/products` | Seller | Create a product |
| `PUT` | `/api/admin/products/{id}` | Seller | Update a product |
| `GET` | `/api/admin/orders?page=0&size=50` | Seller | List customer orders |
| `GET` | `/api/admin/orders/{id}` | Seller | Read one order |
| `PATCH` | `/api/admin/orders/{id}/status` | Seller | Change order status |
| `PATCH` | `/api/admin/orders/{id}/cash-collected` | Seller | Record COD cash after delivery |
| `POST` | `/api/warehouse/sync-stock` | Optional warehouse key | Batch stock update by SKU |

Example order request:

```json
{
  "recipientName": "Customer Name",
  "phone": "+919876543210",
  "deliveryAddress": "Flat 2, Main Road, City, PIN code",
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

The server calculates the order total from current product prices and stock. It ignores client-supplied totals. Order status changes are validated by the server.

## Deploy to Railway

1. Create a new GitHub repository. Upload the **contents of this folder** at the repository root so `Dockerfile`, `pom.xml`, `railway.json`, and `src/` are at the top level.
2. In Railway, add a PostgreSQL service and a web service connected to this GitHub repository.
3. Set the web service variables:

```text
DATABASE_URL=${{Postgres.DATABASE_URL}}
SELLER_USERNAME=your-seller-login
SELLER_PASSWORD=your-unique-password-at-least-12-characters
SESSION_COOKIE_SECURE=true
```

Use the actual Railway PostgreSQL service name in the `DATABASE_URL` reference. The app accepts Railway's `postgresql://` URL and converts it to JDBC internally. Railway provides `PORT`; the included `railway.json` uses `/api/health` as its health check.

`APP_CORS_ALLOWED_ORIGINS` is only needed when a separate mobile/web client calls the JSON API from another origin. Set it to the exact origin(s), comma-separated. `WAREHOUSE_API_KEY` is optional; leave it unset if no warehouse sync client will use the endpoint.

## Operations and limits

- Seller dashboard order refresh is in-app only. It does not send SMS/email notifications while the seller dashboard is closed.
- Seller product forms accept an image URL or an image selected from the device gallery. Uploaded JPG, PNG, WEBP, and GIF images (up to 5 MB) are stored in PostgreSQL by Flyway migration V6, so they survive Railway redeploys.
- The shop shows each customer's latest 10 orders for the current browser session and refreshes every 20 seconds while an order is active. This is session-based order history, not customer OTP/account login; a new device or expired session will not show that history.
- The optional warehouse endpoint is a first-party REST route, not a payment or maps gateway.
- The project is configured for Railway and PostgreSQL but has not been load-tested. Actual capacity depends on the Railway service and database resources.
- Do not commit `.env` or real credentials. Use Railway Variables for production secrets.
