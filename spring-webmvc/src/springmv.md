# HTTP请求处理流程-SpringMVC

第一步：前端控制器dispatcher接受请求

    Client---url--->Dispatcher

第二步：前端控制器去发起handler映射查找请求

    Dispatcher---HttpServletRequest---> HandlerMapping

第三步：处理器映射器查找hanlder并返回HandlerExetuionChain

    Dispatcher <---HandlerExeutionChain---HandlerMapping

第四步：前端控制器发起请求处理器适配器请求执行

    Dispatcher---Handler---> HandlerAdapter

第五步：处理器适配器去调用handler执行

    HandlerAdapter---HttpServletRequest> Handler(Controller)

第六步：处理器处理后返回ModelAndView给HandlerAdapter

    HandlerAdapter <---ModelAndView---Handler(Controller)

第七步：处理器适配器将ModelAndView返回给前端控制器

    Dispatcher <---ModelAndView---HandlerAdapter

第八步：前端控制器请求视图解析器解析ModelAndView

    Dispatcher---ModelAndView---> ViewReslover

第九步：视图解析器解析视图后返回视图View给前端控制器

    Dispatcher <---View---ViewReslover

第十步：前端控制器请求视图要求渲染视图

    Dispatcher--->View--->render

第十一步：前端控制器返回响应

    Response <---Dispatcher