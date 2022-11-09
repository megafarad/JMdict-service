package com.megafarad.jmdictsvc.model.entity

case class Entry(ent_seq: String, k_ele: Option[Seq[K_ele]], r_ele: Seq[R_ele], sense: Seq[Sense])
case class K_ele(keb: String, ke_inf: Option[Seq[String]], ke_pri: Option[Seq[String]])
case class R_ele(reb: String, re_nokanji: Option[String], re_restr: Option[Seq[String]], re_inf: Option[Seq[String]],
                 re_pri: Option[Seq[String]])
case class Sense(stagk: Option[Seq[String]], stagr: Option[Seq[String]], pos: Option[Seq[String]],
                 xref: Option[Seq[String]], ant: Option[Seq[String]], field: Option[Seq[String]],
                 misc: Option[Seq[String]], s_inf: Option[Seq[String]], lsource: Option[Seq[Lsource]],
                 dial: Option[Seq[String]], gloss: Option[Seq[Gloss]], example: Option[Seq[Example]])
case class Gloss(lang: String, content: String, g_gend: Option[String], g_type: Option[String])
case class Lsource(lang: String, content: Option[String], ls_type: Option[String], ls_wasei: Option[String])
case class Example(ex_srce: Exsrce, ex_text: String, ex_sent: Option[Seq[Exsent]])
case class Exsrce(exsrc_type: String, content: String)
case class Exsent(lang: String, content: String)

case class EntryJson(entSeq: String, json: String)
case class EntryIndex(id: String, kanji: String, reading: String, priPoint: Int, meaning: String)