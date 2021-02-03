
Benevis("salam\n")^

agar { 1 &B 2 } [
	Benevis("na!\n")^
]


Sahih v1 = 2^
Sahih v2 = 2^
Sahih i = v1 Zarb v2^

Harf ch = 'a'^

ta {i &K 10 } [
	Benevis("%d\t", i)^
	i YekiBala^
]

Benevis("\n")^

Benevis("%c", ch)^

Sahih s = 0^
Benevis("Gimme a number, ill double it for you.\n")^
Begir("%d", s)^
Benevis("%d", s Zarb 2)^