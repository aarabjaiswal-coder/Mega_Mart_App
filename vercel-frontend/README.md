# Mega Mart Vercel frontend

This folder is the Vercel entry point for the Mega Mart application. Its opening page uses the same Supabase Google sign-in flow as the Spring storefront. Shop, checkout, seller pages, REST routes, and the cart butterfly animation are proxied to the Spring application by `vercel.json`.

## Before deploying

The rewrite in `vercel.json` currently points to `https://mega-mart-app.onrender.com`. If your Render URL changes, update the destination there. The backend must remain online and connected to PostgreSQL; Vercel does not run this Java application or replace the database.

## Deploy

1. Push this repository to GitHub.
2. In Vercel, import the repository.
3. Set **Root Directory** to `vercel-frontend` and leave the framework preset as **Other**. No build command or output directory is needed; the folder contains static files.
4. Deploy. The Vercel URL opens the customer sign-in gateway. After Google sign-in, customers are sent directly into the shop; seller sign-in remains available as a small link below the customer card.

The gateway illustration and styling are local files in this folder. The opening page loads `/js/customer-auth.js` and `/api/auth/config` through the Vercel rewrite, so the customer signs in on the Vercel domain before entering the shop. In Supabase Auth URL Configuration, set the Site URL to your Vercel domain and allow that domain as a redirect URL. Always use one canonical app URL for sign-in and shopping; browser sessions are stored separately for different domains. The cart animation and checkout token verification remain in the Spring app.
