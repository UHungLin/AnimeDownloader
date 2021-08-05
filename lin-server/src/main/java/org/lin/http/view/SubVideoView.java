package org.lin.http.view;


/**
 * @author Lin =￣ω￣=
 * @date 2021/6/24
 */
public class SubVideoView {

	public String bvId; // bilibili 需要大会员才能看的番剧，每一集的 bvId 都不一样
	public int cid;
	public String name;
	public String url;

	@Override
	public String toString() {
		return "SubVideoView{" +
				"bvId='" + bvId + '\'' +
				", cid=" + cid +
				", name='" + name + '\'' +
				", url='" + url + '\'' +
				'}';
	}
}
