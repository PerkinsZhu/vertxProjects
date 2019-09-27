#!/bin/bash
#工作空间，远程服务器要和本地服务器目录保持一致
##########################################################################
# 系统配置项
##########################################################################
remoteWorkSpace="http://127.0.0.1"  #远程nginx地址
bakPath="bak"
versionPath=.version
recordPath=$versionPath/record.txt
##########################################################################
echo "remote working directory:$remoteWorkSpace"

############# 初始化开始 ###################
if [ ! -d "$versionPath" ]
then
	echo "init $versionPath directory"
	mkdir $versionPath
	chmod 
fi

if [ ! -f "$recordPath" ]
then
	echo "init $recordPath"
	echo "" >> $recordPath
fi
############# 初始化结束 ###################

#获取当前运行版本的md5
currentVersion=`tail -n 1 $recordPath| cut -f1 -d " "`
echo "currentVersion:$currentVersion"

unzipAndReload(){
	#执行文件的解压缩、替换、和重启操作
	echo "unzip and restart system"
	fileName=$1
	tar -zxf $fileName
	cd "${fileName%%.*}" && ls -al
	echo "====== restart.sh ============"
	cat restart.sh
	echo "====== restart.sh ============"
	echo "restring system……"
	./restart.sh
	# 回到脚本目录
	cd ../
}

#上一版本备份
doBackUp(){
		directoryName=`date "+%y%m%d%H%M%S"`
		echo "bakPath：$bakPath/$directoryName"
		mkdir -p $bakPath/$directoryName
		mv $1 $bakPath/$directoryName/
		echo "finished back-up"
}

#查看是否有新版本，如果有则自动更新本地版本
checkAndUpdate(){
	# 获取远程版本的md5
	remoteRecord=`curl -s $remoteWorkSpace/record.txt |tail -n 1` 
	remoteVersion=`echo $remoteRecord| cut -f1 -d" "`
	remoteFileName=`echo $remoteRecord| cut -f2 -d" "`
	echo "The latest version：$remoteRecord"
	if [ $remoteVersion == $currentVersion ]
	then
		echo "No need to update！"
	else
		echo "new version published，do update task"
		#备份当前版本
		doBackUp $remoteFileName
		wget $remoteWorkSpace/$remoteFileName
		echo $remoteRecord >> $recordPath
		echo "download file completed"
		unzipAndReload $remoteFileName
	fi
}

#上报当前节点的状态
reportStatus(){
	HOST_IP=$(ifconfig -a|grep -A 2 eth0|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:")
	echo "hello, i am  $HOST_IP。 and my currentVersion is [$currentVersion]"
}




while true
do 
	echo "********************** STASRT TASK ******************************"
	currentVersion=`tail -n 1 $recordPath| cut -f1 -d " "`
	checkAndUpdate
	reportStatus
	echo -e "********************** END TASK ******************************\r\n"
	sleep 5 #每5s检测一次
done
