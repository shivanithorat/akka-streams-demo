# akka-streams-demo

Execute this in 3 different terminals :

```
sbt "runMain streaming.app.Streamer"
sbt "runMain streaming.app.Doubler"
curl http://localhost:9090/
```

Command to watch tcp buffers
```
 watch netstat -n -p tcp
```