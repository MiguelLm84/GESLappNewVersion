package com.example.geslapp.core.requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class InventarioRequest extends StringRequest {

    private Map<String,String> params;

    public InventarioRequest(Response.Listener<String> listener,String IP,String REC) {
        super(Method.POST, "http://"+IP+"/gesl/"+REC+"/inventarios.php", listener, null);
        params = new HashMap<>();
    }
}
