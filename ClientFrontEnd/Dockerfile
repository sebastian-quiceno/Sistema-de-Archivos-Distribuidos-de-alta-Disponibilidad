# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Cache dependencies
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .

# Build argument for the public backend API URL accessible from the browser
ARG VITE_API_URL=http://localhost:8080/api
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# Production stage with Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

