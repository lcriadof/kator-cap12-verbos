package marcombo.lcriadof.capitulo12.verbos.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.github.lcriadof.sofia.gramatica.castellano.verbos.indicativoPresente


fun Application.configureRouting() {

    routing {
        put("/add"){ // [7] paso por body
            val v=call.receive<verbo>().verbo // [8]

            // [9] if
            if  ( (listaDeverbosSoportados.find { e -> e.toString().equals(v) } !=  null) // lo ha encontrado
                &&  (  verbosConjugados.find{ z -> z.verbo.equals(v)  } ==  null ) )  // y no la tenemos ya                           )
            {

                // conjugamos
                var v1 = indicativoPresente(v, 0) // [10]
                var presente = Array<String>(6, { i -> "" }) // [11]
                var x = 0
                v1.conjugarForma() // [12]

                v1.formaConjugada.forEach { it -> // [13]
                    presente[x] = it
                    x++
                }

                var v3: vConjugado = vConjugado(presente[0],
                    presente[1],
                    presente[2],
                    presente[3],
                    presente[4],
                    presente[5]) // [14]

                var v2: rootVerbo = rootVerbo(v, "Presente indicativo", v3) // [15]


                verbosConjugados.add(v2) // [16]
                // fin de conjugación


                call.respond(mensaje("El verbo [$v] se ha incluido en esta sesión")) // [17]
            }else{
                call.respond(mensaje("El verbo [$v] no está soportado actualmente o ya está añadido ")) // [18]]
            }
        }  // fin de put

        get("/conjugar") {  // [3]
            call.respond( verbosConjugados ) // [4] devolvemos JSON
        } // fin de get


        delete() {
            val v = call.receive<verbo>().verbo // [19]

            var n=0
            if (  verbosConjugados.find{ z -> z.verbo.equals(v)  } !=  null ) { // [20]

                // bloque para borrar de verbosConjugados
                var indiceAborrar=0
                n=0
                verbosConjugados.forEach{b ->
                    if (b.verbo.equals(v)){
                        indiceAborrar=n // [21]
                    }
                    n++
                }
                verbosConjugados.removeAt(indiceAborrar) // [22]
                // fin de borrar en verbosConjugados

                call.respond(mensaje("El verbo [${v}] ha sido borrado para esta sesión"))
            }else{
                call.respond(mensaje("El verbo [${v}] no se ha encontrado en esta sesión"))

            }


        } // fin delete




    } // fin routing
}
