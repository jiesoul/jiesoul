@list:
    just --list

@backend-calva:
    clj -M:dev:test:common:backend:calva:kari

@backend-calva-nrepl:
    clj -M:dev:test:common:backend:calva:kari -m nrepl.cmdline

@backend-run-tests:
    clj -M:dev:test:common:backend -m kaocha.runner

# Init node packages.
@init:
    mkdir -p targer
    mkdir -p classes
    rm -rf node_modules
    npm install


# Start frontend auto-compilation

@frontend:
    rm -rf dev-resources
    mkdir -p dev-resources/public/js 
    npx tailwindcss -i ./src/css/app.css -o ./dev/dev-resources/public/index.css
    npm run dev

@tailwind:
    npx tailwindcss -i ./src/css/app.css -o ./dev-resources/public/index.css

@build-uber:
    echo "******* Building fronted ******"
    rm -rf prod-resources
    mkdir -p prod-resources/public/js
    npx tailwindcss -i ./src/css/app.css -o ./prod-resources/public/index.css
    npm shadow-cljs release app
    echo "******* Building backend *******"
    clj -T:build uber

@run-uber:
    PROFILE=prod java -jar target/karimarttila/webstore-standalone.jar

# Update dependencies
@outdated:
    # Backend 
    clj -Moutdated --every --write
    # Frontend
    # Install: npm i -g npm-check-updates
    rm -rf node_modules
    ncu -u
    npm install

@clean:
    rm -rf .cpcache/*
    rm -rf .shadow-cljs/*
    rm -rf target/*
    rm -rf dev-resources/*
    rm -rf prod-resources/*
    rm -rf out/*
    npm install