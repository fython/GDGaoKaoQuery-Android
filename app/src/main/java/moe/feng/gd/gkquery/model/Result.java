package moe.feng.gd.gkquery.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Result {

	public int flag = -1;
	public String msg;
	public CoreResult result;

	public static Result parse(String json) {
		return new Gson().fromJson(json, Result.class);
	}

	public String toJsonString() {
		return new Gson().toJson(this);
	}

	public class CoreResult {

		@SerializedName("zkzh") public String number;
		@SerializedName("yxdm") public String schoolCode;
		@SerializedName("xm") public String name;
		@SerializedName("lbm") public String typeName;
		@SerializedName("pcm") public String orderName;
		@SerializedName("zymc") public String schoolName;

	}

}
