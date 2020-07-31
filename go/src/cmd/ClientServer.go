// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package main

import (
	"C"
	client "github.com/anysync/client/client"
	"fmt"
	"net/http"
	utils "github.com/anysync/client/utils"
)

func startLocalHtmlServer() {
	http.HandleFunc("/rest/", client.RestHandler);
	client.InitServer();
}

//export Start
func Start(){
	utils.HasGui = true;
	utils.InitLogger();
	utils.LoadAppParams()
	fmt.Println("To start local server.")
	client.InitClient();
	startLocalHtmlServer();
}
func main() {
	Start()
}


