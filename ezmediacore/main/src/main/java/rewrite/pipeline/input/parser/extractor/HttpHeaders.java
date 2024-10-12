package rewrite.pipeline.input.parser.extractor;

import com.google.gson.annotations.SerializedName;

public class HttpHeaders{
  @SerializedName("User-Agent")
  public String userAgent;
  @SerializedName("Accept")
  public String accept;
  @SerializedName("Accept-Language")
  public String acceptLanguage;
  @SerializedName("Sec-Fetch-Mode")
  public String secFetchMode;
}
