//Interfaces de authentication
export interface SignInRequestType {
  username: string;
  password: string;
}

export interface SignUpRequestType {
  username: string;
  password: string;
}

//Interfaz respuesta de authentication
export interface AuthResponseType{
    userId: string;
    userName: string;
    token: string;
}
