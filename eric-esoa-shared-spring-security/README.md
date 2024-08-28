## Introduction

This module is used to fetch JWT token and pass into Spring security context holder
the JWT token tenant information.

Both are taken from headers sent via the rest request. It uses filters to get this information and add it to spring security
configuration.

By default, Spring security is configured to allow all requests. It will use its filters to get the JWT and tenant information

If no JWT exists it adds a dummy user.

If not tenant exists it adds a default tenant.

### Forward Filters
This module has interceptor filters to pass the JWT and Tenant to the next service.

This will require each service to add these interceptors to the respective web client.

Current implementation contains interceptors for

1. Spring Webclient
2. Spring RestTemplate
3. Okhttp rest client


### Limitation
This implementation is for spring-boot-starter-web microservices using Tomcat. 
It is not designed for spring reactive microservices that use Netty and webflux.