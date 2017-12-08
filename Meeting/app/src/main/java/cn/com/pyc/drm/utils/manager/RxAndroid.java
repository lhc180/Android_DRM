package cn.com.pyc.drm.utils.manager;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.pyc.drm.bean.BaseBean;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;

import com.alibaba.fastjson.JSON;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @Description: (使用rxAndroid技术 进行接口调用。)
 * @author 李巷阳
 * @date 2016-9-8 下午3:15:40
 * @version V1.0
 */
public class RxAndroid {
	/**
	 * 	 create() , just() , from()   等                 --- 事件产生   
	 *   map() , flapMap() , scan() , filter()  等    --  事件加工
	 *   subscribe()  --  事件消费
	 *   
	 *   事件产生：默认运行在当前线程，可以由 subscribeOn()  自定义线程
	 *   事件加工：默认跟事件产生的线程保持一致, 可以由 observeOn() 自定义线程
	 *   事件消费：默认运行在当前线程，可以有observeOn() 自定义
	 *   
	 *   `subscribeOn` 将作用于 `create` 中的 `OnSubscribe.call()` 方法.
		 `observeOn` 作用于其语法中下一语句的 `Subscriber.onNext` 等函数中.
	 *   
	 *   Observable ：被观察者
	 *   Subscriber ：订阅事件
	 *   Observable与observer通过subscribe()实现订阅关系从而Observable在需要的时候发出事件通知observer
	 *   rxjava回调包括 onNext ，onCompleted ，onError。
	 *   onCompleted：rxjava事件完结。
	 *   onNext：发出，传递参数。表示或许数据完成。
	 *   onError：数据异常时触发。onCompleted和onError二者是互斥得。调用其中一个另一个则不会触发。
	 *   
	 */
	
	
	
	
	
	/**
	 * 
	* @Description: (登陆) 
	* @author 李巷阳
	* @date 2016-9-9 下午1:44:11
	 */
	public static Observable<ReturnDataBean> getLoginChecking(final String uname_set, final String password_set) {

		Observable<ReturnDataBean> observableservice = Observable.create(new Observable.OnSubscribe<ReturnDataBean>() {
			public void call(Subscriber<? super ReturnDataBean> Subscriber) {
				try {
					String returnvalue = RequestHttpManager.getLoginChecking(uname_set, password_set);
					ReturnDataBean bm = JSON.parseObject(returnvalue, ReturnDataBean.class);
					Subscriber.onNext(bm);
					Subscriber.onCompleted();
				} catch (Exception e) {
					Subscriber.onError(e);
				}
			}
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
		return observableservice;
	}
	
	
	/**
	 * 
	* @Description: (获取全部的会议) 
	* @author 李巷阳
	* @date 2016-9-9 下午1:44:25
	 */
	public static Observable<ReturnMeetingRecordDataBean> getAllMeeting(final String LoginPhone,final MechanismBean mmechanismbean) {

		Observable<ReturnMeetingRecordDataBean> observableservice = Observable.create(new Observable.OnSubscribe<ReturnMeetingRecordDataBean>() {
			public void call(Subscriber<? super ReturnMeetingRecordDataBean> Subscriber) {
				try {
					String return_str = RequestHttpManager.getMeetingByOrganization(LoginPhone,  mmechanismbean.getServerAddress(), mmechanismbean.getSZUserName());
					ReturnMeetingRecordDataBean rmrd = JSON.parseObject(return_str, ReturnMeetingRecordDataBean.class);
					Subscriber.onNext(rmrd);
					Subscriber.onCompleted();
				} catch (Exception e) {
					Subscriber.onError(e);
				}
			}
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
		return observableservice;
	}
	
	
	
	
	
	
	
	/**
	 * 
	* @Description: 
	* 1.通过机构码获取机构信息
	* 2.添加机构信息到服务器
	* 3.查询添加机构下面所有的会议 
	* 
	* @author 李巷阳
	* @date 2016-9-9 下午1:44:43
	 */
	public static Observable<ReturnMeetingRecordDataBean> addMechanismAndGetMetting(final TextView mtv_meetingname,final String LoginPhone,final String DataSource) {
		Observable<ReturnMeetingRecordDataBean> observableservice = getMechanismData(DataSource
		)
		.flatMap(new Func1<MechanismBean, Observable<MechanismBean>>() {
			public Observable<MechanismBean> call(MechanismBean mbean) {
				return onAddOrganization(LoginPhone,mbean);
			}
		}).observeOn(AndroidSchedulers.mainThread()).flatMap(new Func1<MechanismBean, Observable<? extends ReturnMeetingRecordDataBean>>() {
			public Observable<? extends ReturnMeetingRecordDataBean> call(MechanismBean arg0) {
				mtv_meetingname.setText(arg0.getServerName());
				return onAddOrganizationGetMetting(LoginPhone,arg0);
			}
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
		return observableservice;
	}
	
	

	
	
	
	
	
	/**
	 * 
	* @Description: (获取扫码对应的机构信息) 
	* @author 李巷阳
	* @date 2016-9-9 上午11:57:43
	 */
	public static Observable<MechanismBean> getMechanismData(final String DataSource) {
		return Observable.create(new Observable.OnSubscribe<MechanismBean>() {
			public void call(Subscriber<? super MechanismBean> Subscriber) {
				try {
					String return_str = RequestHttpManager.getCompany(DataSource);
					final MechanismBean mBean = JSON.parseObject(return_str, MechanismBean.class);
					Subscriber.onNext(mBean);
					Subscriber.onCompleted();
				} catch (Exception e) {
//					Subscriber.onError(e);
					Subscriber.onError(new Throwable("获取机构信息异常."));
				}
			}
		}).subscribeOn(Schedulers.io());
	}
	
	/**
	 * 
	* @Description: (添加机构数据) 
	* @author 李巷阳
	* @date 2016-9-9 上午11:57:43
	 */
	public static Observable<MechanismBean> onAddOrganization(final String LoginPhone,final MechanismBean mBean) {
		return Observable.create(new Observable.OnSubscribe<MechanismBean>() {

			public void call(Subscriber<? super MechanismBean> Subscriber) {

				try {
					String return_str = RequestHttpManager.getWSOrganizationName(LoginPhone, mBean.getServerAddress(), mBean.getServerName(), mBean.getSZUserName());
					BaseBean bbdata = JSON.parseObject(return_str, BaseBean.class);
					if ("true".equals(bbdata.getResult())) {
						if (mBean.getServerAddress() != null) {
							// 切换时，更新端口号
							String[] hostAndPortString = StringUtil.getHostAndPortByResult(mBean.getServerAddress());
							if (hostAndPortString != null) {
								// 保存主机名。eg： video.suizhi.net
								SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
								// 保存端口号。eg：8657
								SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
							}
						}
					}else{
						Subscriber.onError(new Throwable(bbdata.getMsg()));
					}
					
					Subscriber.onNext(mBean);
					Subscriber.onCompleted();
				} catch (Exception e) {
//					Subscriber.onError(e);
					Subscriber.onError(new Throwable("机构创建异常."));
				}
			}

		}).subscribeOn(Schedulers.io());
	}
	
	/**
	 * 
	* @Description: (添加机构后获取机构下所有会议) 
	* @author 李巷阳
	* @date 2016-9-9 下午1:45:54
	 */
	public static Observable<ReturnMeetingRecordDataBean> onAddOrganizationGetMetting(final String LoginPhone,final MechanismBean mmechanismbean) {

		
		return Observable.create(new Observable.OnSubscribe<ReturnMeetingRecordDataBean>() {

			public void call(Subscriber<? super ReturnMeetingRecordDataBean> Subscriber) {

				try {
					String return_str = RequestHttpManager.getMeetingByOrganization(LoginPhone,  mmechanismbean.getServerAddress(), mmechanismbean.getSZUserName());
					ReturnMeetingRecordDataBean rmrd = JSON.parseObject(return_str, ReturnMeetingRecordDataBean.class);
					Subscriber.onNext(rmrd);
					Subscriber.onCompleted();
				} catch (Exception e) {
//					Subscriber.onError(e);
					Subscriber.onError(new Throwable("获取会议异常."));

				}

			}

		}).subscribeOn(Schedulers.io());
	}
	
	
	
	
	
	
}
