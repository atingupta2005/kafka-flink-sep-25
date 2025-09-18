```
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

```
choco install intellijidea-community -y
choco install oraclejdk -y
choco install git -y
```

```
exit
```


```
git clone https://github.com/atingupta2005/kafka-flink-sep-25.git
```

```
exit
```

## Open folder in IDE:
 - kafka-flink-sep-25\hands-on-kafka\3-java-projects\kafka-java