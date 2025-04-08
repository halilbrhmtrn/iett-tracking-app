FROM node:20-alpine as build

WORKDIR /app

# Copy package.json and pnpm-lock.yaml
COPY package*.json pnpm-lock.yaml ./

# Install dependencies
RUN npm install -g pnpm && pnpm install

# Copy app source code
COPY . .

# Expose API URL as an argument so it can be set at build time
ARG VITE_API_URL=/api

# Set environment variable for the build
ENV VITE_API_URL=${VITE_API_URL}

# Build the app
RUN pnpm build

# Production stage
FROM nginx:alpine

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built app from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"] 