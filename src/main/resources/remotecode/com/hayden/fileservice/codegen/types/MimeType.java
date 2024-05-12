package com.hayden.fileservice.codegen.types;

public class MimeType {
  private String type;

  private String value;

  public MimeType() {
  }

  public MimeType(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "MimeType{" + "type='" + type + "'," +"value='" + value + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MimeType that = (MimeType) o;
        return java.util.Objects.equals(type, that.type) &&
                            java.util.Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(type, value);
  }

  public static MimeType.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String type;

    private String value;

    public MimeType build() {
                  MimeType result = new MimeType();
                      result.type = this.type;
          result.value = this.value;
                      return result;
    }

    public MimeType.Builder type(String type) {
      this.type = type;
      return this;
    }

    public MimeType.Builder value(String value) {
      this.value = value;
      return this;
    }
  }
}
