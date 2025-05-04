/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets.assets;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;


public class ModelsDataset {
    public final ModelItemGroup data;

    public ModelsDataset(JSONObject jsonObject) {
        this(jsonObject.toJavaObject(ModelsDatasetBean.class));
    }

    protected ModelsDataset(ModelsDatasetBean bean) {
        Objects.requireNonNull(bean);

        if (bean.data == null || bean.data.isEmpty())
            throw new DatasetKeyException("data");
        data = new ModelItemGroup();
        for (String key : bean.data.keySet()) {
            ModelItem modelItem = bean.data.get(key).toJavaObject(ModelItem.class);
            modelItem.key = key;
            data.add(modelItem);
        }
        data.sort();
    }


    public static class DatasetKeyException extends IllegalArgumentException {
        public DatasetKeyException(String keyName) {
            super("The key \"" + keyName + "\" not found or invalid.");
        }
    }


    protected static class ModelsDatasetBean implements Serializable {
        private HashMap<String, JSONObject> data;

        @JSONField
        public void setData(HashMap<String, JSONObject> data) {
            this.data = data;
        }
    }
}
