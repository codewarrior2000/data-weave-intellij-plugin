{
  a: in0 map $.b,
  b: in0 map { b: $.b, d: $.c },
  c: in0 map { b: $.b, d: "f" },
  d: in0 map { b: $.b, b: [ $.a, $.b ] },
  e: in0 map { b: $.b, b: { a: $.a, b: $.b } },
  f: (["Mariano"]) map $,
  g: in0 map ((value) -> value.b),
  h: in0 map ((value) -> { b: value.b, d: value.c }),
  i: in0 map ((value) -> { b: value.b, d: "f" }),
  j: in0 map ((value) -> { b: value.b, b: [ value.a, value.b ] }),
  k: in0 map ((value) -> { b: value.b, b: { a: value.a, b: value.b } }),
  l: (["Mariano"]) map ((value) -> value),
  m: in0 map { key: $$, value: $ },
  n: in0 map ((key, value) -> key),
  o: in0 map ((key, value) -> { b: value.b, d: key }),
  p: in0 map ((value) -> value),
  p: in0 map (value) -> value,
  q: ([1])map $
}
