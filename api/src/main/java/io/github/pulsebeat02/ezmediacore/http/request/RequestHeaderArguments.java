package io.github.pulsebeat02.ezmediacore.http.request;

/** Basic HTTP request arguments (not all as there are too much!) */
public interface RequestHeaderArguments {

  String HTTP_HEADER = "HTTP/1.0 200 OK";
  String CONTENT_TYPE = "Content-Type: ";
  String CONTENT_LENGTH = "Content-Length: ";
  String DATE = "Date: ";
  String SERVER = "Server: ";
  String USER_AGENT = "User-Agent: ";
}
