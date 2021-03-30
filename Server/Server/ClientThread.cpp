#include "ClientThread.h"

const int MAX_PACK_SIZE = 4096;

ClientThread::ClientThread(SOCKET socket_n, vector <ClientThread*>* clientList, CRITICAL_SECTION* critsection): socket(socket_n), clientListRef(clientList), critSection(critsection) {}

SOCKET ClientThread::getSocket() { return this->socket; }

/*void printPackage(Package pack) {
	cout << endl << "Package:" << endl;
	cout << "type: " << pack.type << endl;
	cout << "size: " << pack.size << endl;
	cout << "from: " << pack.from << endl;
	cout << "to: " << pack.to << endl;
	cout << "message: " << pack.message << endl;
}

char * package2CharArr(Package pack) {
	char rv[MAX_PACK_SIZE];
	//char separator[2];
	//strcpy_s(separator, "\r\n");
	//rv[0] = pack.type;
	sprintf_s(rv, "%d", pack.type);
	//strcat_s(rv, separator);
	strcat_s(rv, "\r\n");
	char num[4];
	sprintf_s(num, "%d", pack.size);
	strcat_s(rv, num);
	//strcat_s(rv, separator);
	strcat_s(rv, "\r\n");
	strcat_s(rv, pack.from);
	//strcat_s(rv, separator);
	strcat_s(rv, "\r\n");
	strcat_s(rv, pack.to);
	//strcat_s(rv, separator);
	strcat_s(rv, "\r\n");
	strcat_s(rv, pack.message);
	strcat_s(rv, "\r\n\0");

	return rv;
}

Package charArr2Package(char datagram[MAX_PACK_SIZE]) {
	Package pack;
	char size[4] = {};
	char from[16] = {};
	char to[2048] = {};
	char data[MAX_PACK_SIZE] = {};
	int i = 3, c = 0, m=0;
	pack.type = (int)datagram[0] - '0';
	while (c < 4) {
		if (datagram[i] == '\r' && datagram[i + 1] == '\n') {
			switch (c) {
			case 0:
				size[m] = '\0';
				break;
			case 1:
				from[m] = '\0';
				break;
			case 2:
				to[m] = '\0';
				break;
			case 3:
				data[m] = '\0';
				break;
			}
			i += 2;
			c++;
			m = 0;
		}
		else {
			switch (c) {
			case 0:
				size[m] = datagram[i];
				break;
			case 1:
				from[m] = datagram[i];
				break;
			case 2:
				to[m] = datagram[i];
				break;
			case 3:
				data[m] = datagram[i];
				break;
			}
			++m;
			++i;
		}
	}
	pack.size = atoi(size);
	strcpy_s(pack.from, from);
	strcpy_s(pack.to, to);
	strcpy_s(pack.message, data);
	return pack;
}

char* prepareDataSequenceForAllChat(Package pack) {
	char rv[MAX_PACK_SIZE];
	sprintf_s(rv, "%s", "0");
	strcat_s(rv, pack.from);
	strcat_s(rv, ": ");
	strcat_s(rv, pack.message);

	return rv;
}

char* prepareDataSequenceForPrivateChat(Package pack) {
	char rv[MAX_PACK_SIZE];
	sprintf_s(rv, "%s", "1");
	strcat_s(rv, pack.from);
	strcat_s(rv, ": ");
	strcat_s(rv, pack.message);

	return rv;
}

char* prepareDataSequenceForConnectedClient(Package pack) {
	char rv[MAX_PACK_SIZE];
	sprintf_s(rv, "%s", "4");
	strcat_s(rv, pack.from);
	strcat_s(rv, " has connected to the server");

	return rv;
}

char* prepareDataSequenceForGroupChat(Package pack) {
	char rv[MAX_PACK_SIZE];
	sprintf_s(rv, "%s", "2");
	strcat_s(rv, "In Group: " );
	strcat_s(rv,pack.to);
	strcat_s(rv, ", ");
	strcat_s(rv, pack.from);
	strcat_s(rv, ": ");
	strcat_s(rv, pack.message);

	return rv;
}*/

void ClientThread :: run() {
	int bufLen = 1024;
	while (true) {
		char RecvBuf[1024] = {};
		int recvResult;

		recvResult = recv(this->socket, RecvBuf, bufLen, 0);
		if (recvResult <= bufLen) {
			cout << "Minden adat fogadva!" << endl;
		}
		else {
			cout << "Nem sikerult minden adatot fogadni!" << endl;
		}
		if (recvResult == SOCKET_ERROR) {
			cout << "Error while receving message: " << WSAGetLastError() << endl;
			closesocket(this->socket);

			//------------------------------------------------------------------
			//Deleting disconnected users from list
			EnterCriticalSection(critSection);
			for (int i = 0; i < clientListRef->size(); ++i) {
				if (!(*clientListRef)[i]->isExited()) {
					clientListRef->erase(find(clientListRef->begin(), clientListRef->end(), (*clientListRef)[i]));
				}
			}
			cout << "A client was removed from the List!" << endl << "There are " << clientListRef->size()+1 << " users online!" << endl;
			LeaveCriticalSection(critSection);
			break;
		}
		printf("Message: %s\n", RecvBuf);
		echoMSG(RecvBuf);
		//Package pack = charArr2Package(RecvBuf);
		/*if (pack.type == 4) {
			//Login
			strcpy_s(this->username, pack.from);
			char messageclientdata[MAX_PACK_SIZE] = {};
			strcpy_s(messageclientdata, prepareDataSequenceForConnectedClient(pack));
			int c = sendMSG2All(messageclientdata, pack.from);
			if (c) {
				cout << "Mindenkinek sikeresen tovabbitva lett az uzenet!" << endl;
			}
			//cout << this->username;
		}
		else if (pack.type == 3) {
			//File
		}
		else if (pack.type == 2) {
			//Group
			char message[MAX_PACK_SIZE] = {};
			strcpy_s(message, prepareDataSequenceForGroupChat(pack));
			int v = sendGroupMessage(message, pack.from, pack.to);
		}
		else if (pack.type == 1) {
			//Private
			char message[MAX_PACK_SIZE] = {};
			strcpy_s(message, prepareDataSequenceForPrivateChat(pack));
			int v = sendPrivateMessage(message, pack.to);
		}
		else if (pack.type == 0){
			//All
			char message[MAX_PACK_SIZE] = {};
			strcpy_s(message, prepareDataSequenceForAllChat(pack));
			int v = sendMSG2All(message,pack.from);
			if (v) {
				cout << "Mindenkinek sikeresen tovabbitva lett az uzenet!" << endl;
			}
		}*/
	}
	//---------------------------------------------
	// When the application is finished sending, close the socket.
	cout << "Finished sending. Closing socket." << endl;
	closesocket(this->socket);
}

int ClientThread::echoMSG(char message[]) {
	cout << "Sending datagram...\n";
	int bufLen = strlen(message) + 1, sentBytes = 0, rv = 0;
	for (int i = 0; i < clientListRef->size(); ++i) {
			int recvResult = send((*clientListRef)[i]->socket, message, bufLen, 0);
			sentBytes += recvResult;
			if (recvResult == bufLen) {
				printf("Minden adat tovabbitva!\n");
			}
			else {
				printf("Nem sikerult minden adatot tovabbitani!\n");
			}
			if (recvResult == SOCKET_ERROR) {
				printf("Client missing!");
			}
	}

	if ((sentBytes / clientListRef->size()) == bufLen) {
		rv = 1;
	}
	return rv;
	
}

/*int ClientThread :: sendMSG2All(char message[], char from[]) {
	// Send a datagram
	printf("Sending a datagram to All Chat...\n");
	int bufLen = strlen(message) + 1, sentBytes = 0, rv = 0;
	for (int i = 0; i < clientListRef->size(); ++i) {
		if (strcmp((*clientListRef)[i]->username,from)){
			int recvResult = send((*clientListRef)[i]->socket, message, bufLen, 0);
			sentBytes += recvResult;
			if (recvResult == bufLen) {
				printf("Minden adat tovabbitva!\n");
			}
			else {
				printf("Nem sikerult minden adatot tovabbitani!\n");
			}
			if (recvResult == SOCKET_ERROR) {
				printf("Client missing!");
			}
		}
	}

	if ((sentBytes / clientListRef->size()) == bufLen) {
		rv = 1;
	}
	return rv;
}

int ClientThread::sendPrivateMessage(char message[], char to[]) {
	// Send a datagram
	printf("Sending a datagram to All Chat...\n");
	int bufLen = strlen(message) + 1, recvResult=0;
	for (int i = 0; i < clientListRef->size(); ++i) {
		if (!strcmp((*clientListRef)[i]->username, to)){
			recvResult = send((*clientListRef)[i]->socket, message, bufLen, 0);
			if (recvResult == bufLen) {
				printf("Minden adat tovabbitva!\n");
			}
			else {
				printf("Nem sikerult minden adatot tovabbitani!\n");
			}
			if (recvResult == SOCKET_ERROR) {
				printf("Client missing!");
			}
		}
	}
	return recvResult;
}

int ClientThread::sendGroupMessage(char message[], char from[], char to[]) {
	int vesszo = strlen(to);
	to[vesszo] = ',';
	to[vesszo + 1] = '\0';
	char temp[17];
	int m=0;
	// Send a datagram
	printf("Sending a datagram to Group...\n");
	int bufLen = strlen(message) + 1, sentBytes = 0, rv = 0;
	for (int i = 0; i < vesszo + 1; ++i) {
		if (to[i] == ',') {
			temp[m] = '\0';
			for (int i = 0; i < clientListRef->size(); ++i) {
				if (!strcmp((*clientListRef)[i]->username, temp)) {
					int recvResult = send((*clientListRef)[i]->socket, message, bufLen, 0);
					sentBytes += recvResult;
					if (recvResult == bufLen) {
						printf("Minden adat tovabbitva!\n");
					}
					else {
						printf("Nem sikerult minden adatot tovabbitani!\n");
					}
					if (recvResult == SOCKET_ERROR) {
						printf("Client missing!");
					}
				}
			}
			m = 0;
		}
		else {
			temp[m] = to[i];
			m++;
		}
	}

	if ((sentBytes / clientListRef->size()) == bufLen) {
		rv = 1;
	}
	return rv;
}*/