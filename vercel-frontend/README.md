# Mega Mart Vercel frontend

This folder is a static Vercel entry point for the existing Mega Mart Spring Boot app. The gateway is served by Vercel. Store, checkout, seller pages, REST routes, and the existing cart butterfly animation are proxied to the Spring application, so its Thymeleaf pages and session login continue to work.

## Before deploying

The rewrite in `vercel.json` currently points to `https://mega-mart-app.onrender.com`. If your Render URL changes, update the destination there. The backend must remain online and connected to PostgreSQL; Vercel does not run this Java application or replace the database.

## Deploy

1. Push this repository to GitHub.
2. In Vercel, import the repository.
3. Set **Root Directory** to `vercel-frontend` and leave the framework preset as **Other**. No build command or output directory is needed; the folder contains static files.
4. Deploy. The Vercel URL opens the customer/seller gateway. The **Start shopping** and **Seller sign in** links continue into the backend through the rewrite.

The gateway illustration and styling are local files in this folder. Requests for application routes and assets that are not present here pass through to the Spring app. The cart animation remains in the backend's `src/main/resources/static/js/app.js` and plays when products are added.
