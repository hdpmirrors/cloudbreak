package com.sequenceiq.freeipa.api.model.image;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    private final String date;

    private final String description;

    private final String os;

    private final String osType;

    private final String uuid;

    private final Map<String, Map<String, String>> imageSetsByProvider;

    @JsonCreator
    public Image(
            @JsonProperty(value = "date", required = true) String date,
            @JsonProperty(value = "description", required = true) String description,
            @JsonProperty(value = "os", required = true) String os,
            @JsonProperty(value = "uuid", required = true) String uuid,
            @JsonProperty(value = "images", required = true) Map<String, Map<String, String>> imageSetsByProvider,
            @JsonProperty("os_type") String osType) {
        this.date = date;
        this.description = description;
        this.os = os;
        this.osType = osType;
        this.uuid = uuid;
        this.imageSetsByProvider = imageSetsByProvider;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getOs() {
        return os;
    }

    public String getUuid() {
        return uuid;
    }

    public String getOsType() {
        return osType;
    }

    public Map<String, Map<String, String>> getImageSetsByProvider() {
        return imageSetsByProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Image image = (Image) o;
        return Objects.equals(date, image.date)
                && Objects.equals(description, image.description)
                && Objects.equals(os, image.os)
                && Objects.equals(osType, image.osType)
                && Objects.equals(uuid, image.uuid)
                && Objects.equals(imageSetsByProvider, image.imageSetsByProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description, os, osType, uuid, imageSetsByProvider);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Image{");
        sb.append("date='").append(date).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", os='").append(os).append('\'');
        sb.append(", osType='").append(osType).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", imageSetsByProvider=").append(imageSetsByProvider);
        sb.append('}');
        return sb.toString();
    }
}
