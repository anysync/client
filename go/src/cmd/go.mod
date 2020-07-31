module github.com/anysync/client/cmd

go 1.12

require (
	github.com/VividCortex/ewma v1.1.1 // indirect
	github.com/anysync/client/client v0.0.0
	github.com/anysync/client/utils v0.0.0
	github.com/h2non/filetype v1.1.0 // indirect
	github.com/mattn/go-sqlite3 v1.14.0 // indirect
	github.com/panjf2000/ants v1.3.0 // indirect
	github.com/rclone/rclone v1.52.2 // indirect
)

replace github.com/anysync/client/utils => ../pkg/utils

replace github.com/anysync/client/client => ../pkg/client
