# BocDataCollection

  BocDataCollection这是chinapex中银项目埋点后台接收服务程序.使用了scala，Finagle框架搭建，接收到用户信息后放进消息队列对用户的在线情况进行分析。将在实时上线用户和下线用户分地区记录进counter，同时还提供当前在线用户数，记录在Map中，根据不同的operate_id标志唯一用户，map记录数量就是当前在线用户总数，5分钟不操作就判定用户下线。
  
  
  1.DataCollectionServer主程序，启动HTTP服务
  2.BOCService  进行业务逻辑处理
  3.BOCEventLoader 消息队列事务处理
  4.BocLogger 日志记录
  
  运行环境jdk 1.7,启动命令参看start.sh
