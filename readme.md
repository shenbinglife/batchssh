# BatchSSH Tool  
批量执行SSH命令，多线程并发执行。

## 使用方法  
1. 修改jar包同级目录下的配置文件：config.properties  
    ```
    hosts=192.168.40.9, 192.168.40.10
    pwd=root1234
    user=root
    # ssh port, default 22 if not set
    port=22
    # ssh timeout mills,default 10 min if not set
    timeout=600000
    ```

2. 批量执行命令功能  
    ```$bash
    java -jar batchssh-1.0.jar exec "yum install -y unzip"
    ```

3. 批量执行SCP功能  
    ```$bash
    java -jar batchssh-1.0.jar scp "D:/config" /opt
    ```