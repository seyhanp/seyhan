@echo off

call .\conf\init.bat

rem stop the instance
java -cp ./lib/stopper.jar com.seyhanproject.stopper.Stopper %http_port%
if exist RUNNING_PID (
   del RUNNING_PID
)

