package scalaz

/** Liskov substitutability : A better <:<
  */

trait Liskov[-A,+B] {
  def subst[F[-_]](p: F[B]) : F[A]

  final def *[+[+_,+_],C,D](that: Liskov[C,D]) : Liskov[A+C,B+D] = Liskov.lift2(this,that)
  final def andThen[C](that: Liskov[B,C]) : Liskov[A,C] = Liskov.trans(that,this)
  final def compose[C](that: Liskov[C,A]) : Liskov[C,B] = Liskov.trans(this,that)
}

object Liskov { 

  /** A convenient type alias for Liskov */
  type <~:[-A,+B] = Liskov[A,B] 

  /** A flipped alias, for those used to their arrows running left to right */
  type :~>[+B,-A] = Liskov[A,B]

  /** Lift scala's subtyping relationship */
  implicit def isa[A,B >: A] : A <~: B = new (A <~: B) {
    def subst[F[-_]](p: F[B]) : F[A] = p
  }

  /** We can witness equality by using it to convert between types */
  implicit def witness[A,B](lt: A <~: B) : A => B = {
    type f[-X] = X => B
    lt.subst[f](identity)
  }

  /** Subtyping is reflexive */
  implicit def refl[A]: (A <~: A) = new (A <~: A) {
    def subst[F[_]](p:F[A]): F[A]= p
  }

  /** Subtyping is transitive */
  def trans[A,B,C](f: B <~: C, g: A <~: B) : A <~: C = {
    type g[-X] = X <~: C
    g.subst[g](f)
  }
 
  /** Subtyping forms a category */
  implicit def LiskovCategory: Category[<~:] = new Category[<~:] {
    def id[A] : (A <~: A) = refl[A]
    def compose[A,B,C](bc: B <~: C, ab: A <~: B) : (A <~: C) = trans(bc,ab)
  }

  /** We can lift subtyping into any covariant type constructor */
  def co[T[+_],A,A2](a: A <~: A2) : (T[A] <~: T[A2]) = {
    type a[-X] = T[X] <~: T[A2]
    a.subst[a](refl)
  }

  def co1_2[T[+_,_],Z,A,B](a: A <~: Z): T[A,B] <~: T[Z,B] = {
    type a[-X] = T[X,B] <~: T[Z,B]
    a.subst[a](refl)
  }

  def co2_2[T[_,+_],Z,A,B](a: B <~: Z): T[A,B] <~: T[A,Z] = {
    type a[-X] = T[A,X] <~: T[A,Z]
    a.subst[a](refl)
  }

  def co1_3[T[+_,_,_],Z,A,B,C](a: A <~: Z): T[A,B,C] <~: T[Z,B,C] = {
    type a[-X] = T[X,B,C] <~: T[Z,B,C]
    a.subst[a](refl)
  }

  def co2_3[T[_,+_,_],Z,A,B,C](a: B <~: Z): T[A,B,C] <~: T[A,Z,C] = {
    type a[-X] = T[A,X,C] <~: T[A,Z,C]
    a.subst[a](refl)
  }

  def co3_3[T[_,_,+_],Z,A,B,C](a: C <~: Z): T[A,B,C] <~: T[A,B,Z] = {
    type a[-X] = T[A,B,X] <~: T[A,B,Z]
    a.subst[a](refl)
  }

  def co1_4[T[+_,_,_,_],Z,A,B,C,D](a: A <~: Z): T[A,B,C,D] <~: T[Z,B,C,D] = {
    type a[-X] = T[X,B,C,D] <~: T[Z,B,C,D]
    a.subst[a](refl)
  }

  def co2_4[T[_,+_,_,_],Z,A,B,C,D](a: B <~: Z): T[A,B,C,D] <~: T[A,Z,C,D] = {
    type a[-X] = T[A,X,C,D] <~: T[A,Z,C,D]
    a.subst[a](refl)
  }

  def co3_4[T[_,_,+_,_],Z,A,B,C,D](a: C <~: Z): T[A,B,C,D] <~: T[A,B,Z,D] = {
    type a[-X] = T[A,B,X,D] <~: T[A,B,Z,D]
    a.subst[a](refl)
  }

  def co4_4[T[_,_,_,+_],Z,A,B,C,D](a: D <~: Z): T[A,B,C,D] <~: T[A,B,C,Z] = {
    type a[-X] = T[A,B,C,X] <~: T[A,B,C,Z]
    a.subst[a](refl)
  }

  /** lift2(a,b) = co1_2(a) compose co2_2(b) */
  def lift2[T[+_,+_],A,A2,B,B2](
    a: A <~: A2,
    b: B <~: B2
  ): (T[A,B] <~: T[A2,B2]) = {
    type a[-X] = T[X,B2] <~: T[A2,B2]
    type b[-X] = T[A,X]  <~: T[A2,B2]
    b.subst[b](a.subst[a](refl))
  }

  /** lift3(a,b,c) = co1_3(a) compose co2_3(b) compose co3_3(c) */
  def lift3[T[+_,+_,+_],A,A2,B,B2,C,C2](
    a: A <~: A2,
    b: B <~: B2,
    c: C <~: C2
  ): (T[A,B,C] <~: T[A2,B2,C2]) = {
    type a[-X] = T[X,B2,C2] <~: T[A2,B2,C2]
    type b[-X] = T[A,X, C2] <~: T[A2,B2,C2]
    type c[-X] = T[A,B, X ] <~: T[A2,B2,C2]
    c.subst[c](b.subst[b](a.subst[a](refl)))
  }

  /** lift4(a,b,c,d) = co1_3(a) compose co2_3(b) compose co3_3(c) compose co4_4(d) */
  def lift4[T[+_,+_,+_,+_],A,A2,B,B2,C,C2,D,D2](
    a: A <~: A2,
    b: B <~: B2,
    c: C <~: C2,
    d: D <~: D2
  ): (T[A,B,C,D] <~: T[A2,B2,C2,D2]) = {
    type a[-X] = T[X,B2,C2,D2] <~: T[A2,B2,C2,D2]
    type b[-X] = T[A,X, C2,D2] <~: T[A2,B2,C2,D2]
    type c[-X] = T[A,B, X ,D2] <~: T[A2,B2,C2,D2]
    type d[-X] = T[A,B, C ,X ] <~: T[A2,B2,C2,D2]
    d.subst[d](c.subst[c](b.subst[b](a.subst[a](refl))))
  }

  /** We can lift subtyping into any contravariant type constructor */
  def contra[T[-_],A,A2](a: A <~: A2) : (T[A2] <~: T[A]) = {
    type a[-X] = T[A2] <~: T[X]
    a.subst[a](refl)
  }

  // binary 
  def contra1_2[T[-_,_],Z,A,B](a: A <~: Z) : (T[Z,B] <~: T[A,B]) = { 
    type a[-X] = T[Z,B] <~: T[X,B]
    a.subst[a](refl)
  }

  def contra2_2[T[_,-_],Z,A,B](a: B <~: Z) : (T[A,Z] <~: T[A,B]) = { 
    type a[-X] = T[A,Z] <~: T[A,X]
    a.subst[a](refl)
  }

  // ternary
  def contra1_3[T[-_,_,_],Z,A,B,C](a: A <~: Z) : (T[Z,B,C] <~: T[A,B,C]) = { 
    type a[-X] = T[Z,B,C] <~: T[X,B,C]
    a.subst[a](refl)
  }

  def contra2_3[T[_,-_,_],Z,A,B,C](a: B <~: Z) : (T[A,Z,C] <~: T[A,B,C]) = { 
    type a[-X] = T[A,Z,C] <~: T[A,X,C]
    a.subst[a](refl)
  }

  def contra3_3[T[_,_,-_],Z,A,B,C](a: C <~: Z) : (T[A,B,Z] <~: T[A,B,C]) = { 
    type a[-X] = T[A,B,Z] <~: T[A,B,X]
    a.subst[a](refl)
  }

  def contra1_4[T[-_,_,_,_],Z,A,B,C,D](a: A <~: Z) : (T[Z,B,C,D] <~: T[A,B,C,D]) = { 
    type a[-X] = T[Z,B,C,D] <~: T[X,B,C,D]
    a.subst[a](refl)
  }

  def contra2_4[T[_,-_,_,_],Z,A,B,C,D](a: B <~: Z) : (T[A,Z,C,D] <~: T[A,B,C,D]) = { 
    type a[-X] = T[A,Z,C,D] <~: T[A,X,C,D]
    a.subst[a](refl)
  }

  def contra3_4[T[_,_,-_,_],Z,A,B,C,D](a: C <~: Z) : (T[A,B,Z,D] <~: T[A,B,C,D]) = { 
    type a[-X] = T[A,B,Z,D] <~: T[A,B,X,D]
    a.subst[a](refl)
  }

  def contra4_4[T[_,_,_,-_],Z,A,B,C,D](a: D <~: Z) : (T[A,B,C,Z] <~: T[A,B,C,D]) = { 
    type a[-X] = T[A,B,C,Z] <~: T[A,B,C,X]
    a.subst[a](refl)
  }

  /** Lift subtyping into a Function1-like type 
    * liftF1(a,r) = contra1_2(a) compose co2_2(b)
    */
  def liftF1[F[-_,+_],A,A2,R,R2](
    a: A <~: A2,
    r: R <~: R2
  ): (F[A2,R] <~: F[A,R2]) = {
    type a[-X] = F[A2,R2] <~: F[X,R2]
    type r[-X] = F[A2,X]  <~: F[A,R2]
    r.subst[r](a.subst[a](refl))
  }

  /** Lift subtyping into a function 
    * liftF2(a,b,r) = contra1_3(a) compose contra2_3(b) compose co3_3(c)
    */
  def liftF2[F[-_,-_,+_],A,A2,B,B2,R,R2](
    a: A <~: A2,
    b: B <~: B2,
    r: R <~: R2
  ): (F[A2,B2,R] <~: F[A,B,R2]) = {
    type a[-X] = F[A2,B2,R2] <~: F[X,B2,R2]
    type b[-X] = F[A2,B2,R2] <~: F[A,X, R2]
    type r[-X] = F[A2,B2,X ] <~: F[A,B, R2]
    r.subst[r](b.subst[b](a.subst[a](refl)))
  }

  /** Lift subtyping into a function 
    * liftF3(a,b,c,r) = contra1_4(a) compose contra2_4(b) compose contra3_4(c) compose co3_4(d)
    */
  def liftF3[F[-_,-_,-_,+_],A,A2,B,B2,C,C2,R,R2](
    a: A <~: A2,
    b: B <~: B2,
    c: C <~: C2,
    r: R <~: R2
  ): (F[A2,B2,C2,R] <~: F[A,B,C,R2]) = {
    type a[-X] = F[A2,B2,C2,R2] <~: F[X,B2,C2,R2]
    type b[-X] = F[A2,B2,C2,R2] <~: F[A,X, C2,R2]
    type c[-X] = F[A2,B2,C2,R2] <~: F[A,B, X, R2]
    type r[-X] = F[A2,B2,C2,X ] <~: F[A,B, C, R2]
    r.subst[r](c.subst[c](b.subst[b](a.subst[a](refl))))
  }

  /** Lift subtyping into a function */
  def liftF4[F[-_,-_,-_,-_,+_],A,A2,B,B2,C,C2,D,D2,R,R2](
    a: A <~: A2,
    b: B <~: B2,
    c: C <~: C2,
    d: D <~: D2,
    r: R <~: R2
  ): (F[A2,B2,C2,D2,R] <~: F[A,B,C,D,R2]) = {
    type a[-X] = F[A2,B2,C2,D2,R2] <~: F[X,B2,C2,D2,R2]
    type b[-X] = F[A2,B2,C2,D2,R2] <~: F[A,X, C2,D2,R2]
    type c[-X] = F[A2,B2,C2,D2,R2] <~: F[A,B, X, D2,R2]
    type d[-X] = F[A2,B2,C2,D2,R2] <~: F[A,B, C, X, R2]
    type r[-X] = F[A2,B2,C2,D2,X ] <~: F[A,B, C, D, R2]
    r.subst[r](d.subst[d](c.subst[c](b.subst[b](a.subst[a](refl)))))
  }

  // def bracket[A,B](_: A <~: B, _: A :~> B): Leibniz[A,B] = Leibniz.force[A,B]
}
