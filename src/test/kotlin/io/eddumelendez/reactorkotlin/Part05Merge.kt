package io.eddumelendez.reactorkotlin

import io.eddumelendez.reactorkotlin.domain.User
import io.eddumelendez.reactorkotlin.repository.ReactiveUserRepository
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class Part05Merge {

    val MARIE = User("mschrader", "Marie", "Schrader")
    val MIKE = User("mehrmantraut", "Mike", "Ehrmantraut")

    val repository1 =
        ReactiveUserRepository(500) // !!!!!!! this is delayed - that's why MARIE, MIKE always come before users in this Flux of repository1
    val repository2 = ReactiveUserRepository(MARIE, MIKE)

    @Test
    fun mergeWithInterleave() {
        val flux = mergeFluxWithInterleave(repository1.findAll(), repository2.findAll())

        flux
            .doOnNext { println("${it.firstname} ${it.lastname}") }
            .test()
            .expectNext(MARIE, MIKE, User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
            .verifyComplete()
    }

    // TODO Merge flux1 and flux2 values with interleave
    fun mergeFluxWithInterleave(flux1: Flux<User>, flux2: Flux<User>): Flux<User> {
        return flux1.mergeWith(flux2) // same as Flux.merge(flux2, flux1)
    }

    @Test
    fun mergeWithNoInterleave() {
        val flux = mergeFluxWithNoInterleave(repository1.findAll(), repository2.findAll())

        flux
            .doOnNext { println("${it.firstname} ${it.lastname}") }
            .test()
            .expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL, MARIE, MIKE)
            .verifyComplete()
    }

    // TODO Merge flux1 and flux2 values with no interleave (flux1 values and then flux2 values)
    fun mergeFluxWithNoInterleave(flux1: Flux<User>, flux2: Flux<User>): Flux<User> {
        return flux1.concatWith(flux2)
    }

    @Test
    fun multipleMonoToFlux() {
        val skylerMono = repository1.findFirst()
        val marieMono = repository2.findFirst()

        val flux = createFluxFromMultipleMono(skylerMono, marieMono)

        flux.test()
            .expectNext(User.SKYLER, MARIE)
            .verifyComplete()
    }

    // TODO Create a Flux containing the value of mono1 then the value of mono2
    fun createFluxFromMultipleMono(mono1: Mono<User>, mono2: Mono<User>): Flux<User> {
        return Flux.concat(mono1, mono2)
    }
}
