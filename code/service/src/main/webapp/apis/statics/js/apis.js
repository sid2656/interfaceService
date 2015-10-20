var apis=[
		    new API("1.取得首页精选全部信息","/api/babyrun/v2/index/query",
		    		new PARAM("userId","用户id")
		    ),
		    new API("2.取得首页更多精选Exp列表信息","/api/babyrun/v2/index/expmore",
		    		new PARAM("userId","当前用户id"),
		    		new PARAM("skip","分页跳过数量")
		    ),
		    new API("3.取得首页广场兴趣全部信息","/api/babyrun/v2/second/tags"),
		    new API("4.取得二级界面推荐育儿详情列表信息","/api/babyrun/v2/second/childs",
		    		new PARAM("tagId","首页点击的tag"),
		    		new PARAM("skip","分页跳过数量")
		    ),
		    new API("5.取得二级界面推荐达人信息","/api/babyrun/v2/second/users"),
		    new API("6.取得二级界面推荐地点信息","/api/babyrun/v2/second/pois",
		    		new PARAM("poiCity","所属城市名称")
		    ),
		    new API("7.取得兴趣标签对应的三级界面经验信息","/api/babyrun/v2/third/tagexps",
		    		new PARAM("tagId","标签ID"),
		    		new PARAM("skip","分页跳过数量")
		    ),
		    new API("8.取得分组达人三级界面列表信息","/api/babyrun/v2/third/expertusers",
		    		new PARAM("expertId","分组信息ID"),
		    		new PARAM("userId","当前用户ID")
		    ),
		    new API("9.取得POI三级界面详情列表信息","/api/babyrun/v2/third/poiexps",
		    		new PARAM("poiId","地点id")
		    ),
		    new API("10.取得经验贴参与人员列表信息","/api/babyrun/v2/exp/usermsgs",
		    		new PARAM("expId","经验贴ID")
		    ),
		    new API("11.取得经验帖对应的评论信息","/api/babyrun/v2/exp/comments",
		    		new PARAM("expId","经验贴ID"),
		    		new PARAM("userId","当前用户ID")
		    ),
		    new API("12.取得发现首页全部信息","/api/babyrun/v2/index/brandkinds"),
		    new API("13.发现首页搜索接口","/api/babyrun/v2/index/brandkindselect",
		    		new PARAM("key","搜索关键词")
		    ),
		    new API("14.取得搜索后得相关品牌信息列表","/api/babyrun/v2/brand/search",
		    		new PARAM("key","搜索关键词"),
		    		new PARAM("size","显示个数（非必选）")
		    ),
		    new API("15.取得热门产品列表","/api/babyrun/v2/brand/hot"),
		    new API("16.取得热门品类列表","/api/babyrun/v2/kind/hot"),
		    new API("17.取得品类下品牌信息及列表","/api/babyrun/v2/kind/brandandexp",
		    		new PARAM("kindId","品类ID"),
		    		new PARAM("skip","分页跳过的个数")
		    ),
		    new API("18.取得产品信息及列表","/api/babyrun/v2/brand/brandandexp",
		    		new PARAM("brandId","品牌ID"),
		    		new PARAM("skip","分页跳过的个数")
		    ),
		    new API("19.取得品类下的全部品牌","/api/babyrun/v2/kind/brands",
		    		new PARAM("kindId","品类ID")
		    ),
		    new API("20.取得贴纸信息","/api/babyrun/v2/paster/type",
		    		new PARAM("type","贴纸类型")
		    ),
		    new API("21.取得标签列表信息","/api/babyrun/v2/tags/list"),
		    new API("22.取得商品全部信息","/api/babyrun/v2/brand/list",
		    		new PARAM("skip","分页跳过数量")		
		    ),
		    new API("23.查找商品信息","/api/babyrun/v2/brand/search",
		    		new PARAM("key","搜索关键词"),
		    		new PARAM("skip","分页跳过数量")
		    ),
		    new API("24.发布经验贴","/api/babyrun/v2/exp/save",
		    		new PARAM("content","经验贴内容"),
		    		new PARAM("readCount","查看数量"),
		    		new PARAM("joinCount","互动数量"),
		    		new PARAM("history","时间"),
		    		new PARAM("poiUid","地点UID"),
		    		new PARAM("userId","用户ID"),
		    		new PARAM("imgUrl","主图地址"),
		    		new PARAM("isReport","是否被举报"),
		    		new PARAM("isDel","是否被删除")
		    ),
		    new API("25.取得关注用户经验贴信息","/api/babyrun/v2/index/followeeexps",
		    		new PARAM("userId","用户ID")
		    ),
		    new API("26.添加经验帖喜欢","/api/babyrun/v2/exppraise/save",
		    		new PARAM("userId","用户ID"),
		    		new PARAM("expId","经验ID")
		    ),
		    new API("27.取消经验帖喜欢","/api/babyrun/v2/exppraise/cancel",
		    		new PARAM("userId","用户ID"),
		    		new PARAM("expId","经验ID")
		    ),
		    new API("28.添加经验帖评论","/api/babyrun/v2/expcomment/save",
		    		new PARAM("userId","用户ID"),
		    		new PARAM("expId","经验ID"),
		    		new PARAM("content","评论内容"),
		    		new PARAM("toUserId","被评论用户的ID(非必填)")
		    ),
		    new API("29.删除经验帖评论","/api/babyrun/v2/expcomment/delete",
		    		new PARAM("commentId","需要删除的评论ID")
		    ),
		    new API("30.删除经验帖","/api/babyrun/v2/exp/del",
		    		new PARAM("expId","经验ID")
		    ),
		    new API("31.取得用户所有经验按数量信息","/api/babyrun/v2/exp/counts",
		    		new PARAM("userId","用户ID")
		    ),
//		    new API("32.取得用户所有经验按发布顺序显示","/api/babyrun/v2/exp/query",
//		    		new PARAM("")
//		    ),
		    new API("55.获取海报列表","/api/babyrun/v2/poster/list"),
		    new API("56.获取LOGO列表","/api/babyrun/v2/logo/list",
		    		new PARAM("skip","分页跳过数量")
		    ),
		    new API("57.获取LOGO列表","/api/babyrun/v2/kind/kindsandbrands")
		    
	   	] ;