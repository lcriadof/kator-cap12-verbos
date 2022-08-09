package marcombo.lcriadof.capitulo12.verbos.plugins

import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.security.*
import kotlin.text.Charsets.UTF_8
import java.io.*


fun getMd5Digest(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))


// variables de autentificación
val myRealm = "Access to the '/' path"
val userTable: Map<String, ByteArray> = mapOf(
    "jetbrains" to getMd5Digest("jetbrains:$myRealm:foobar"),
    "lcriadof" to getMd5Digest("lcriadof:$myRealm:prueba1234"),
    "admin" to getMd5Digest("admin:$myRealm:password")
)

// para JSON de los mensajes
data class mensaje (val texto:String)

// variables de sesión
data class CartSession(val userID: String, var userLogin: String)
var numeroUsurActivos=0
var numeroUsuariosHistóricos=0
var parar=false


// variables de verbos
var verbosConjugados: MutableList<rootVerbo> = mutableListOf()
var listaDeverbosSoportados: MutableList<String> = mutableListOf( "calcular","cambiar","caminar","cancelar",
    "cansarse","cantar", "caracterizar","casar","celebrar","cenar","coincidir", "comer",
    "consistir", "correr", "corresponder", "cortar", "cumplir", "curiosear",
    "abastar", "ser" )

data class vConjugado(val yo:String,val tu:String, val el:String, val nosotros:String, val vosotros:String, val ellos:String)
data class rootVerbo (val verbo:String, val forma:String, val conjugacion:vConjugado)
data class verbo (val etiquetaverbo:String, val verbo:String)



fun Application.configureSecurity() {

    authentication {
        digest("auth-digest") {
            realm = myRealm
            digestProvider { userName, realm ->
                userTable[userName]
            }
        }
    }

    install(Sessions) { // [4]
        val secretSignKey = hex("6819b57a326945c1968f45236589")
        header<CartSession>("cart_session", directorySessionStorage(File("build/.sessions"))) {
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }


    routing {
        authenticate("auth-digest") {
            get("/login") {
                var userCandidato=call.principal<UserIdPrincipal>()?.name

                if (parar==false){
                    numeroUsurActivos++
                    numeroUsuariosHistóricos++
                    call.sessions.set(CartSession(userID = "user"+numeroUsuariosHistóricos, userLogin=userCandidato.toString() ))
                    call.respond(mensaje("Acceso concedido ${call.sessions.get<CartSession>()}"))
                }else{
                    call.respond(mensaje("El servidor no acepta más usuarios"))
                }
           } // fin de login
        }

        get("/logout") {
            val cartSession = call.sessions.get<CartSession>()
            if (cartSession != null) {
                call.sessions.clear<CartSession>()
                call.respond(mensaje("Sesión cerrada del usuario ${cartSession.userLogin}"))
                numeroUsurActivos--
            } else {
                call.respond(mensaje("Sesión inexistente"))
            }
        }

        get("/activas") {
            call.respond(mensaje("Sesiones activas $numeroUsurActivos")) // devolvemos JSON

        }

        get("/parar") {
            parar=true
            call.respond(mensaje("Se activado la orden para no aceptar más sesiones")) // devolvemos JSON
        }



    } // fin de routing


} //  fin de Application.configureSecurity
