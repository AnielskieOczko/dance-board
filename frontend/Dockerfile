FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Webpack bundled JS
COPY build/kotlin-webpack/js/productionExecutable/DanceBoard-frontend.js /usr/share/nginx/html/

# Static resources (index.html, styles.css)
COPY build/processedResources/js/main/ /usr/share/nginx/html/

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
