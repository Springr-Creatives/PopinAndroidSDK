package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class ProductModel {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("url")
    public String url;
    @SerializedName("price")
    public int price;
    @SerializedName("image")
    public String image;
    @SerializedName("external_id")
    public String externalId;
    @SerializedName("variants")
    public List<ProductVariantModel> variants;

    @Keep
    public class ProductVariantModel {
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
        @SerializedName("value")
        public String value;
        @SerializedName("image")
        public String image;
    }

}
