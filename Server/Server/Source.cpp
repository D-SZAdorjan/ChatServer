#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <stdio.h>
#include <string.h>
#include "ClientThread.h"

#pragma comment(lib, "ws2_32.lib")

void main() {
	//Initialize Winsock:
	WSADATA wsaData;
	int iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != NO_ERROR) {
		cout << "Error at WSA Startup!" << endl;
		return;
	}

	//-----------------------------------------------------------------
	// Create a SOCKET for listening for
	// incoming connection requests.
	SOCKET listenSocket;
	listenSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (listenSocket == INVALID_SOCKET) {
		cout << "Error at creating the socket" << WSAGetLastError() << endl;
		WSACleanup();
		return;
	}

	//-----------------------------------------------------------------
	// The sockaddr_in structure specifies the address family,
	// IP address, and port for the socket that is being bound.
	sockaddr_in service;
	service.sin_family = AF_INET;
	service.sin_addr.s_addr = inet_addr("127.0.0.1");
	service.sin_port = htons(13000);

	//Binding
	if (bind(listenSocket, (SOCKADDR*)&service, sizeof(service)) == SOCKET_ERROR) {
		cout << "Binding failed!" << endl;
		closesocket(listenSocket);
		WSACleanup();
		return;
	}

	//-----------------------------------------------------------------
	// Listen for incoming connection requests.
	// on the created socket
	if (listen(listenSocket, 1) == SOCKET_ERROR) {
		cout << "Error on socket listening!" << endl;
		closesocket(listenSocket);
		WSACleanup();
		return;
	}

	//-----------------------------------------------------------------
	//Critical section
	CRITICAL_SECTION critSection;
	InitializeCriticalSection(&critSection);

	//-----------------------------------------------------------------
	//Creating the ClientThreadList
	vector<ClientThread*> clientList;

	//-----------------------------------------------------------------
	// Create a SOCKET for accepting incoming requests.
	SOCKET acceptSocket;
	cout << "Waiting for client to connect..." << endl;

	//-----------------------------------------------------------------
	// Accept the connection.
	bool acceptHappened;
	do {
		acceptHappened = false;
		acceptSocket = accept(listenSocket, NULL, NULL);
		if (acceptSocket == INVALID_SOCKET) {
			if (WSAGetLastError() != 10004) {
				cout << "accept failed: " << WSAGetLastError() << endl;
			}
			else {
				cout << "A client disconnected!"<<endl;
			}
			//closesocket(listenSocket);
			//WSACleanup();
			return;
		}
		else {
			printf("Client connected.\n");
			ClientThread* client = new ClientThread(acceptSocket, &clientList, &critSection);
			client->start();
			clientList.push_back(client);
			cout << "A new client was added to the List!" << endl << "There are " << clientList.size() << " users online!" << endl;
			acceptHappened = true;
			//break;
		}
		//--------------------------------------------------------------
		// Call the recvfrom function to receive datagrams
		// on the bound socket.
		printf("Receiving datagrams...\n");
	} while(acceptHappened);

	//------------------------------------------------------------------
	//Deleting disconnected users from list
	EnterCriticalSection(&critSection);
	for (int i = 0; i < clientList.size(); ++i){
		if (!clientList[i]->isExited()) {
			clientList.erase(find(clientList.begin(), clientList.end(), clientList[i]));
		}
	}
	LeaveCriticalSection(&critSection);
	cout << "A client was removed from the List!" << endl << "There are " << clientList.size() << " users online!" << endl;
	//------------------------------------------------------------------
	//Closing socket
	printf("Finished sending. Closing socket.\n");
	closesocket(listenSocket);

	//---------------------------------------------
	// Clean up and quit.
	printf("Exiting.\n");
	WSACleanup();
	return;
}