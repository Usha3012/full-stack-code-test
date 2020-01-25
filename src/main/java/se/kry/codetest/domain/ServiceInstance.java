package se.kry.codetest.domain;

import java.util.Objects;

public class ServiceInstance {
    private Integer id;
    private String url;
    private String name;
    private String status;
    public ServiceInstance(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ServiceInstance(String url, String name){
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return url.equals(that.url) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
