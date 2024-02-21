package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("LombokGetterMayBeUsed")
public final class FilterMap {

    @Getter
    @Setter
    private String filterName = null;

    @Setter
    @Getter
    private String servletName = null;

    @Getter
    @Setter
    private String urlPattern = null;

    public String toString() {
        StringBuilder sb = new StringBuilder("FilterMap[");
        sb.append("filterName=");
        sb.append(this.filterName);
        if (servletName != null) {
            sb.append(", servletName=");
            sb.append(servletName);
        }
        if (urlPattern != null) {
            sb.append(", urlPattern=");
            sb.append(urlPattern);
        }
        sb.append("]");
        return (sb.toString());
    }
}
