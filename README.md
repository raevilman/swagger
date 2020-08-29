## What?

I use this repo to host swagger files.  

Following are swaggers currently being hosted:  
1. [Pet Store](https://raevilman.github.io/swagger/)
2. [Events Counter](https://raevilman.github.io/swagger/?url=events-counter.yaml)



---

This project has following two parts

- Swagger UI being served as GitHub pages from the `docs` folder
- Swagger generation utility written in Scala 2.12 with maven


## Dev

### Structure

There is `SwaggerGenerator` scala class to generate swagger's yaml file.  

For generating swagger for different APIs, separate `SwaggerGenerator` class will be written in separate package.  

Eg:  `com.therdnotes.swagger.eventscounter` contains `SwaggerGenerator` class for generating swagger for EventsCounter API.  

### Locally testing the swagger file

On run, `SwaggerGenerator` generates a yaml file.  

To view this file locally in swagger ui, one can 

- Run below commands to serve static files locally using http server from python

```
cd docs
python3.8 -m http.server 8090
```
- Open `http://localhost:8090/` in your browser
> Don't forget the trailing slash
- Refresh the web page in browser every time you run this utility to load the latest swagger.yaml file.