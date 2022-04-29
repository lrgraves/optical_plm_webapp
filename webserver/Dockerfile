FROM python as docs

RUN pip install mkdocs
COPY ./spark ./
RUN mkdocs build

# Pull nginx base image
FROM nginx:latest

# Copy custom configuration file from the current directory
COPY nginx.conf mime.types /etc/nginx/

# Copy static assets into var/www
COPY --from=docs ./site/ /www/data/

# Start up nginx server
CMD ["nginx", "-g", "daemon off;"]