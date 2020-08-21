package com.iconflux.brokingbulls.myUtils;

import com.iconflux.brokingbulls.models.PropertyProjectPermission;

public interface OnPermissionReceived {
    void postData(PropertyProjectPermission model);
}
