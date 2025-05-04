/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets.assets;

import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.github.promeg.pinyinhelper.Pinyin;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;


/** One Model Item is corresponding to one certain local Spine model.
 */
public class ModelItem implements Serializable {
    @JSONField(serialize = false)
    public String key;
    @JSONField
    public String assetId;
    @JSONField
    public String type;
    @JSONField
    public String name;
    @JSONField
    public String appellation;
    @JSONField
    public JSONArray sortTags;

    // Lazy generated fields:
    private ModelAssetAccessor accessor;
    private String pinyinQuanpin;
    private String pinyinSuoxie;

    protected static final String[] extensions = {".atlas", ".png", ".skel"};

    private ModelItem() {
    }

    /** Gets the Model Asset Accessor of the model's asset files.
     * @return A Model Asset Accessor instance.
     */
    @JSONField(serialize = false)
    public ModelAssetAccessor getAccessor() {
        if (accessor == null)
            accessor = new ModelAssetAccessor(key);
        return accessor;
    }

    /** Gets the 拼音全拼 (Pinyin Quanpin, full Pinyin transcription) of the model's name.
     * @return A String.
     */
    @JSONField(serialize = false)
    public String getPinyinQuanpin() {
        if (pinyinQuanpin == null)
            pinyinQuanpin = Pinyin.toPinyin(name, "");
        return pinyinQuanpin;
    }

    /** Gets the 拼音缩写 (Pinyin Suoxie, abbreviate Pinyin transcription) of the model's name.
     * @return A String.
     */
    @JSONField(serialize = false)
    public String getPinyinSuoxie() {
        if (pinyinSuoxie == null) {
            String quanpin = Pinyin.toPinyin(name, " ").trim();
            if (!quanpin.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (String word : quanpin.split("\\s+")) {
                    builder.append(word.charAt(0));
                }
                pinyinSuoxie = builder.toString();
            } else {
                pinyinSuoxie = "";
            }
        }
        return pinyinSuoxie;
    }

    /** Verifies the integrity of the necessary fields of this {@code ModelItem}.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. Both {@code assetDir} and {@code type} are not {@code null}.
     *          2. The {@code ModelAssetAccessor} is available.
     */
    @JSONField(serialize = false)
    public boolean isValid() {
        return this.key != null;
    }

    /** Verifies the existence of the target local directory.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. The method {@code isValid()} returns {@code true}.
     *          2. The local {@code assetDir} exists.
     */
    @JSONField(serialize = false)
    public boolean isExisted() {
        return isValid();
    }

    /** Verifies the integrity of the related local files.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. The method {@code isExisted()} returns {@code true}.
     *          2. All the local files exist.
     */
    @JSONField(serialize = false)
    public boolean isChecked() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelItem) {
            return ((ModelItem) obj).key.equals(key);
        }
        return false;
    }


    /** The Model Asset Accessor providing methods to get the resource files of the model's asset files.
     * @since ArkPets 2.2
     */
    public static class ModelAssetAccessor {
        private final ArrayList<String> list;
        private final HashMap<String, ArrayList<String>> map;

        public ModelAssetAccessor(String key) {
            ArrayList<String> list = new ArrayList<>();
            File kf = new File(key);
            list.add(kf.getName() + ".png");
            list.add(kf.getName() + ".atlas");
            list.add(kf.getName() + ".json");
            this.list = list;
            HashMap<String, ArrayList<String>> map = new HashMap<>();
            map.put(".png", new ArrayList<>(List.of(kf.getName() + ".png")));
            map.put(".atlas", new ArrayList<>(List.of(kf.getName() + ".atlas")));
            map.put(".skel", new ArrayList<>(List.of(kf.getName() + ".json")));
            this.map = map;
        }

        public String[] getAllFiles() {
            return list.toArray(new String[0]);
        }

        public String[] getAllFilesOf(String fileType) {
            if (map.containsKey(fileType))
                return map.get(fileType).toArray(new String[0]);
            Logger.warn("Asset", "getAllFilesOf() Method has returned an empty list.");
            return new String[0];
        }

        public String getFirstFileOf(String fileType) {
            String[] all = getAllFilesOf(fileType);
            if (all != null && all.length > 0)
                return all[0];
            Logger.warn("Asset", "getFirstFileOf() Method has returned null.");
            return null;
        }

        public JSONObject getFileMapAsJSONObject() {
            JSONObject map = new JSONObject();
            map.putAll(this.map);
            return map;
        }

        public boolean isAvailable() {
            return !map.isEmpty() && !list.isEmpty();
        }
    }


    /** The Model Property Extractor specializing in extracting a specified property.
     * @param <T> The type of the specified property, typically {@code String}.
     * @since ArkPets 2.2
     */
    public interface PropertyExtractor<T> extends Function<ModelItem, Set<T>> {
        /** Extracts the specified property of the given Model Item.
         * @param modelItem The given Model Item.
         * @return A value {@link Set} of the extracted property.
         */
        @Override
        Set<T> apply(ModelItem modelItem);

        PropertyExtractor<String> ASSET_ITEM_KEY             = item -> item.key           == null ? Set.of() : Set.of(item.key);
        PropertyExtractor<String> ASSET_ITEM_TYPE            = item -> item.type          == null ? Set.of() : Set.of(item.type);
        PropertyExtractor<String> ASSET_ITEM_SORT_TAGS       = item -> new HashSet<>(item.sortTags.toJavaList(String.class));
    }


    /** The Model Prefab storing the user prefab of the specific model.
     * @since ArkPets 3.5
     */
    public static class AssetPrefab {
        // Attr feature is still WIP
        @JSONField(defaultValue = "0.2", serialize = false, deserialize = false)
        public float   initial_position_x;
        @JSONField(defaultValue = "0.2", serialize = false, deserialize = false)
        public float   initial_position_y;
        @JSONField(defaultValue = "false", serialize = false, deserialize = false)
        public boolean transparent_mode;

        public AssetPrefab() {
        }

        public AssetPrefab(float x, float y, boolean transparent) {
            this.initial_position_x = x;
            this.initial_position_y = y;
            this.transparent_mode = transparent;
        }
    }
}
