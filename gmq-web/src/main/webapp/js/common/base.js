/*!
* base.js
*/
//定义JS命名空间、防止JS签名重复
var Namespace = Namespace || new Object();
Namespace.register = function(path){var arr = path.split("."),ns = "";;for(var i=0;i<arr.length;i++){if(i>0){ns += ".";}ns += arr[i];eval("if(typeof(" + ns + ")=='undefined'){" + ns + " = new Object();}");}};

//基础框架
Namespace.register("XUI");
(function(){
	String.prototype.trim=function(){return this.replace(/^\s+|\s+$/g, "");};
	String.prototype.ltrim=function(){return this.replace(/^\s+/g, "");};
	String.prototype.rtrim=function(){return this.replace(/\s+$/g, "");};
	String.prototype.firstIndexOf=function(prefix){var index=-1,temp = -1,str = this;while((temp = str.lastIndexOf(prefix))>=0){str = str.substring(0,temp);index = temp;}return index;};
	String.prototype.endsWith=function(suffix){var C=this.length;var D=suffix.length;if(D>C)return false;return (D==0 || this.substr(C-D,D)==suffix);};
	String.prototype.replaceAll=function(regex,replacement) {var raRegExp = new RegExp(regex,"g");return this.replace(raRegExp,replacement);};
	String.prototype.chineseLength=function(){ return this.replace(/[^\x00-\xff]/g,"**").length;};
	String.prototype.remove=function(startIndex,removeLength) {var s='';if(startIndex>0)s=this.substring(0,startIndex);if(startIndex+removeLength<this.length)s+=this.substring(startIndex+removeLength,this.length);return s;};
	Date.prototype.format=function(mask){var d = this;var zeroize = function (value, length) {if (!length) length = 2;value = String(value);for (var i = 0, zeros = ''; i < (length - value.length); i++) {zeros += '0';}return zeros + value;};return mask.replace(/"[^"]*"|'[^']*'|\b(?:d{1,4}|m{1,4}|yy(?:yy)?|([hHMstT])\1?|[lLZ])\b/g, function($0) {switch($0) {case 'd':return d.getDate();  case 'dd':return zeroize(d.getDate());case 'ddd':return ['Sun','Mon','Tue','Wed','Thr','Fri','Sat'][d.getDay()];case 'dddd':return ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'][d.getDay()];case 'M':return d.getMonth() + 1;case 'MM':return zeroize(d.getMonth() + 1);case 'MMM':return ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'][d.getMonth()];  case 'MMMM':return ['January','February','March','April','May','June','July','August','September','October','November','December'][d.getMonth()];case 'yy':return String(d.getFullYear()).substr(2);case 'yyyy':return d.getFullYear();case 'h':return d.getHours() % 12 || 12;  case 'hh':return zeroize(d.getHours() % 12 || 12);  case 'H':return d.getHours();case 'HH':return zeroize(d.getHours());case 'm':return d.getMinutes();case 'mm':return zeroize(d.getMinutes());case 's':return d.getSeconds();case 'ss':return zeroize(d.getSeconds());case 'l':return zeroize(d.getMilliseconds(), 3);case 'L':var m = d.getMilliseconds();if (m > 99) m = Math.round(m / 10);return zeroize(m);case 'tt':return d.getHours() < 12 ? 'am' : 'pm';case 'TT':return d.getHours() < 12 ? 'AM' : 'PM';case 'Z':return d.toUTCString().match(/[A-Z]+$/);default:return $0.substr(1, $0.length - 2);}});};
	XUI.isne = function(str){return str==null||str=="undefined"||str.trim()=="";};
	XUI.vmContainer = function(){return $("#vmContainer");};
	
	XUI.constructor = new Array();//构造方法列表
	XUI.addConstructor = function(func){XUI.constructor.push(func);};//新增构造方法
	XUI.init = function(panel){//初始化执行所有构造方法
		var execute = function(func){try{eval("func();");}catch(e){console.log(e);}};
		for(var func in XUI.constructor){execute(function(){XUI.constructor[func](panel);});}
		$("SPAN.pageLoad").each(function(i,o){execute(function(){eval($(o).remove().attr("data-pageLoad"));});});
		var navBar = $("#vmContainer #navBar");
		$("BODY.admin>DIV.content>UL.breadcrumb").html(navBar.html());
		$("BODY.admin>DIV.content>DIV.header>H1.page-title").text(navBar.attr("title"));
	};
})();


Namespace.register("XUI.tip");
(function(){
	XUI.tip.init = function(panel) {//鼠标提示
		var content = "BODY";
		if(panel != null) {content = panel;}
		$(content).find("*[rel=popover]").each(function(i,o){var contentEl = $(o).attr("data-content"),contentFunc=null;if(!XUI.isne(contentEl) && contentEl.indexOf("javascript:")==0){$(o).removeAttr("data-content");contentFunc = function(){var text = eval(contentEl.substr(11));$(o).attr("data-content",text);return text;};}$(o).removeAttr("rel").popover({content:contentFunc});});
		$(content).find("*[rel=tooltip]").each(function(i,o){$(o).removeAttr("rel").tooltip();});
		$(content).find("*.tip").each(function(i,o){$(o).removeClass("tip").attr("title",$(o).text().trim());});
	};
	XUI.addConstructor(XUI.tip.init);
})();


Namespace.register("XUI.table");
(function(){
	XUI.table.resizable = function(panel){
		var table = "TABLE.table thead th,TABLE.table thead td";
		if(panel != null) {tbale = $(panel).find(table);}
		var oldCursor = $(table).attr("title","点击排序").css('cursor');
		var resizeWidth = 2;//拖动偏离像素
		//表格拖动
		$(table).unbind("mousemove").bind("mousemove",function(ev){
			var th = $(this);
			if (th.prevAll().length < 1 || th.nextAll().length < 1) {return;}//不给第一列和最后一列添加效果
			//距离表头边框线左右4像素才触发效果
			var left = th.offset().left;
			var effect = (ev.clientX - left) < resizeWidth || (th.outerWidth() - (ev.clientX - left)) < resizeWidth;
			if (effect) {th.css('cursor','col-resize');} else{th.css('cursor',oldCursor);}
		}).unbind("mousedown").bind("mousedown", function(ev) {
			var dd = new Object();
			dd.obj = $(this);//TH对象
			var pos = dd.obj.offset();
			dd.table = dd.obj.parent().parent().parent();//Table对象
			var effect = (ev.clientX - pos.left) < resizeWidth || (dd.obj.outerWidth() - (ev.clientX - pos.left)) < resizeWidth;
			if (!effect || dd.obj.prevAll().length < 1 || dd.obj.nextAll().length < 1) {return;}//跳过第一列和最后一列
			dd.line = $("<div class='dragLine'></div>");//竖线
			dd.line.css({ "height": dd.table.height(), "top": dd.table.offset().top,"left":ev .clientX,"display":"block" });//显示调整线
			if (ev.clientX - pos.left < dd.obj.width() / 2){dd.obj = dd.obj.prev();}//记录当前TH
			var x = ev.clientX;dd.rangeStart = dd.table.offset().left;dd.rangeEnd = dd.rangeStart + dd.table.width();
			$("body").append(dd.line).bind("selectstart",function(){
				return false;
			}).bind("mousemove",function(event){
				if(dd.rangeStart < event.clientX && dd.rangeEnd > event.clientX){dd.line.css({"left":event.clientX});dd.deltaX = event.clientX - x;}
			}).bind("mouseup",function(event){
				dd.line.remove();dd.obj.width(Math.max(40, dd.obj.width() + dd.deltaX));
				var defaultWidth = parseInt(dd.table.attr("defaultWidth"));
				dd.table.width(Math.max(defaultWidth, dd.table.width() + dd.deltaX));
				$("body").unbind("mousemove").unbind("mouseup").unbind("selectstart").css('cursor','default').attr("unselectable","off").css("MozUserSelect","");
			}).css('cursor','col-resize').attr("unselectable","on").css("MozUserSelect","none");
		});
	};
	XUI.addConstructor(XUI.table.resizable);
	
	//表格排序
	XUI.table.sorting = function(panel){
		var sorting = "TABLE.table .sorting,TABLE.table .sorting_ASC,TABLE.table .sorting_DESC";
		if(panel != null) {sorting = $(panel).find(sorting);}
		$(sorting).unbind("click").bind("click",function(){
			var field = $(this).attr("data-orderField"),type = $(this).attr("data-orderFieldType");
			var form = XUI.vmContainer().find("FORM")[0];
			if(form["orderField"]==null){$(form).append("<input type='hidden' name='orderField'/>");}
			if(form["orderFieldType"]==null){$(form).append("<input type='hidden' name='orderFieldType'/>");}
			form["orderField"].value = field;
			form["orderFieldType"].value = type;
			$(form).submit();
		});
	};
	XUI.addConstructor(XUI.table.sorting);
})();


Namespace.register("XUI.window");
(function(){
	//打开窗口
	XUI.window.open = function(url,params,method,conf){
		var open = function(data) {
			//JSON数据
			try{if(data != null && !XUI.isne(data.result)){alert(data.result);return;}}catch(e){}
			var modal = XUI.window.openDialog(data,conf);
			modal.attr("data-url",url).attr("data-params",params).attr("data-method",method);
		};
		
		XUI.ajax.send(url,params,method,null,{callback:open});
	};
	
	XUI.window.queue = new Array();//当前已经打开的窗口
	//打开对话框窗口
	XUI.window.openDialog = function(content,conf){
		var def_opts ={title:'弹出窗口',width : 'auto',height : 'auto',buttons:[],draggable:true,closeable:true};
		var options=$.extend(true, {}, def_opts, conf || {});
		
		var modal = $("<div class='modal hide window'></div>");
		var modalHeader = $("<div class='modal-header'></div>");
		var backdrop = $('<div class="modal-backdrop"/>');
		if(options.closeable) {
			var closeBtn = $("<a class='close'>×</a>");
			closeBtn.bind("click",function(){modal.remove();backdrop.remove();XUI.window.queue.pop();});
			modalHeader.append(closeBtn);
		}
		modalHeader.append("<h3>"+options["title"]+"</h3>");
		
		var modalBody = $("<div class='modal-body' style='padding:10px'></div>");
		modalBody.append(content);
		var modalFooter = "";
		modalBody.find("DIV.bottomBar>button,DIV.bottomBar>A").each(function(i,o){
			options.buttons.push(o);
		});
		if(options.buttons != null && options.buttons.length > 0) {
			modalFooter = $("<div class='modal-footer' style='padding:10px'></div>");
			for(var btn in options.buttons){
				modalFooter.append(options.buttons[btn]);
			}
		}
		XUI.window.queue.push(modal);
		var zIndex = 1050+XUI.window.queue.length;
		backdrop.css("z-index",zIndex);
		modal.append(modalHeader).append(modalBody).append(modalFooter).appendTo(document.body).before(backdrop);
		setTimeout(function(){
			modal.css({"z-index":zIndex+1,"width":options.width,"height":options.height,"margin-left":"0px"});
			var left =($(window).width() - modal.width())/2 - 63;
			var top =($(window).height() - modal.height())/2;
			modal.css({"left":left,"top":top}).fadeIn("slow");
		},50);
		
		if(options.draggable){modal.draggable({handle: modalHeader,containment:'BODY'});}
		XUI.init(modal);
		return modal;
	};
	//关闭顶层窗口
	XUI.window.close = function(){
		var length = XUI.window.queue.length;
		if(length>0){XUI.window.queue[length-1].find('DIV.modal-header>A.close').click();}
	};
	//关闭所有窗口
	XUI.window.closeAll = function(){
		$("DIV.modal-backdrop,DIV.modal").remove();
		XUI.window.queue = new Array();
	};
	XUI.window.refresh = function(){//刷新顶层窗口
		if(XUI.window.queue.length <= 0) {return false;}
		var modal = XUI.window.queue[XUI.window.queue.length-1];
		var opts = {"url": modal.attr("data-url"),"params": modal.attr("data-params"),"method": modal.attr("data-method")};
		if(opts.url == null){return false;}
		var refresh = function(data) {
			try{if(data != null && !XUI.isne(data.result)){alert(data.result);return false;}}catch(e){}
			var content = modal.find("DIV.modal-body");
			XUI.init(content.html(data));
		};
		XUI.ajax.send(opts.url,opts.params,opts.method,null,{callback:refresh});
		return true;
	};
})();

Namespace.register("XUI.form");
(function(){
	XUI.form.page = function(num) {
		var form = XUI.vmContainer().find("FORM")[0];
		if(form["pageSize"]==null){$(form).append("<input type='hidden' name='pageSize'/>");}
		if(form["currentPage"]==null){$(form).append("<input type='hidden' name='currentPage'/>");}
		form["pageSize"].value = XUI.vmContainer().find("#page-pageSize").val();
		form["currentPage"].value = num;
		$(form).submit();
	};
	
	XUI.form.setPageSize = function(dv,inp,evt){
		if(inp.value == "") {return;}
		inp.value = inp.value.replace(/\D+/g,'');
		if(inp.value < 1) {inp.value = dv;}
		if(evt.keyCode==13){XUI.form.page(1);}
	};
	
	XUI.form.showAll = function(count){
		$("#page-pageSize").val(count);
		XUI.form.page(1);
	};
	XUI.form.init = function(panel) {
		var from = "FORM";
		if(panel != null) {from = $(panel).find(from);}
		$(from).unbind("submit").bind("submit",function(){//阻止表单默认提交事件
			var dataType = $(this).attr("data-resultType");
			var isJSON = (!XUI.isne(dataType) && dataType.toUpperCase() == "JSON");
			var refresh = ($(this).attr("refresh") == "true"?true:false);
			XUI.ajax.send($(this).attr("action"),$(this).serialize(),$(this).attr("method"),dataType,{
				"closeWindow":isJSON||refresh,"refresh":isJSON||refresh
			});
			return false;
		});
	};
	XUI.addConstructor(XUI.form.init);
})();

Namespace.register("XUI.ajax");
(function(){
	//内部私有方法
	var successMethod = function(data,dataType){//请求成功
		try{if(data != null && !XUI.isne(data.result)) {alert(data.result);return;}} catch (e) {}
		XUI.init(XUI.vmContainer().html(data));
	},errorMethod = function(xhr,url,requestType){//请求错误
		var message = {"code":"failure","result":"请求URL["+url+"],发生"+xhr["statusText"]+"错误."};
		var dataType = XUI.isne(requestType) ? "" : requestType.toUpperCase();
		switch (xhr["status"]) {
			case 403: message["result"]="您无权访问本页面.";break;
			case 404: message["result"]="不存在的URL["+url+"]";break;
			case 500: message["result"]="服务器内部错误."+xhr["statusText"];break;
		}
		if(dataType == "JSON" || (!XUI.isne(url) && url.toUpperCase().endsWith(".JSON"))) {successMethod(message,dataType);}
		else{successMethod("<div class='alert'>"+message["result"]+"</div>",dataType);}
	},vmData = new Object();
	
	//页面跳转href方式
	XUI.gotoPageHref= function(url,params,method){
		self.location.href = url;
	};
	
	//页面跳转ajax方式
	XUI.gotoPage = function(url,params,method){
		vmData = {"url":url,"params":params,"method":method};
		XUI.refreshVM();
	};
	//刷新当前VM
	XUI.refreshVM = function(){
		XUI.ajax.send(vmData.url,vmData.params,vmData.method);return true;
	};
	
	//Ajax请求
	XUI.ajax.send = function(url,params,method,type,config){
		var cfg = $.extend(true, {}, {"closeWindow":false,"refresh":false,"callback":null}, config || {});
		var ajaxOptions = {
			url:contextPath + url,data:params,
			success:function(data){
				var type = XUI.isne(this.dataType) ? "" : this.dataType.toUpperCase();
				if(cfg.callback != null) {
					cfg.callback(data);
				} else {
					successMethod(data,type);
				}
				if(type=="JSON" && data.code == "failure") {return;}
				if(cfg.closeWindow == true) {
					XUI.window.close();
				}
				if(cfg.refresh == true && XUI.window.refresh()==false) {
					XUI.refreshVM();
				}
			}
		};
		if(!XUI.isne(method)) {ajaxOptions["type"] = method;}
		if(!XUI.isne(type)) {ajaxOptions["dataType"] = type;}
		jQuery.ajax(ajaxOptions);
	};
	
	//以JSON的方式执行DELETE方法
	XUI.ajax["delete"] = function(url,params,config) {
		var cfg = $.extend(true, {}, {"confirm":"您确定要删除吗?"}, config || {});
		if(window.confirm(cfg.confirm)) {
			XUI.ajax.send(url,params,"DELETE","",config);
		}
	};
	
	//请求时界面遮罩
	XUI.ajax.mask = function(msg){var info = msg;if($("#winModal,#loadInfo").length == 0) {var msg = (!XUI.isne(info)) ? info:"正在努力为您获取数据,请稍候...";$("body").append("<div id='winModal' class='modal-backdrop fade in' style='z-index:9999'></div><div id='loadInfo'>"+msg+"</div>");}};
	XUI.ajax.unmask = function(){$("#winModal,#loadInfo").remove();};
	
	//Ajax全局设置
	jQuery.ajaxSetup({
		type:"POST",cache:false,
		beforeSend :function(xhr){XUI.ajax.mask();},
		error :function(xhr, ts,et){errorMethod(xhr, this.url,this.dataType);this.complete(xhr, ts);},
		complete :function(xhr, ts){XUI.ajax.unmask();}
	});
})();