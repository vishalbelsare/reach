package org.clulab.reach.grounding

/**
  * Trait for defining constants used by grounding and entity checking code.
  *   Written by Tom Hicks. 10/22/2015.
  *   Last Modified: Move no species value and test here.
  */
object ReachKBConstants {

  /** The default namespace string for KBs. */
  val DefaultNamespace: String = "uaz"

  /** The string used to separate a namespace and an ID. */
  val NamespaceIdSeparator: String = ":"

  /** Constant which represents the lack of a species in a KB entry. */
  val NoSpeciesValue: String = ""

  /** Tell whether the given string is the special no-species constant or not. */
  def isNoSpeciesValue (species:String): Boolean = (species == NoSpeciesValue)


  /** File Path to the directory which holds the entity knowledge bases. */
  val KBDirFilePath = "src/main/resources/org/clulab/reach/kb"

  /** Resource Path to the directory which holds the entity knowledge bases. */
  val KBDirResourcePath = "/org/clulab/reach/kb"


  /** Filename of the cellular location file generated by the entity checker. */
  val GendCellLocationFilename = "biopax-cellular_component.tsv"
  /** Prefix string for cellular location IDs generated by the entity checker. */
  val GendCellLocationPrefix = "UA-BP-CC-"

  /** Filename of the small molecule file generated by the entity checker. */
  val GendChemicalFilename = "biopax-simple_chemical.tsv"
  /** Prefix string for small molecule IDs generated by the entity checker. */
  val GendChemicalPrefix = "UA-BP-SC-"

  /** Filename of the protein/family file generated by the entity checker. */
  val GendProteinFilename = "biopax-gene_or_gene_product.tsv"
  /** Prefix string for protein/family IDs generated by the entity checker. */
  val GendProteinPrefix = "UA-BP-GGP-"


  /** Filename of the static bio processes file. */
  val StaticBioProcessFilename = "bio_process.tsv"

  /** Filename of the static cellular location file. */
  val StaticCellLocationFilename = "GO-subcellular-locations.tsv"

  /** Filename of the alternate static cellular location file. */
  val StaticCellLocation2Filename = "uniprot-subcellular-locations.tsv"

  /** Filename of the static small molecule (chemical) file. */
  val StaticChemicalFilename = "PubChem.tsv"

  /** Filename of the static small molecule (chemical) file from ChEBI. */
  val StaticChemicalFilenameChebi = "chebi.tsv"

  /** Filename of the static small molecule (drug) file. */
  val StaticDrugFilename = "hms-drugs.tsv"

  /** Filename of the static small molecule (metabolite) file. */
  val StaticMetaboliteFilename = "hmdb.tsv"

  /** Filename of the static protein file. */
  val StaticProteinFilename = "uniprot-proteins-0_F.tsv"
  val StaticProteinFilename2 = "uniprot-proteins-G_P.tsv"
  val StaticProteinFilename3 = "uniprot-proteins-Q_Z.tsv"

  /** Filename containing protein fragments from the Protein Ontology */
  val StaticProteinFragmentFilename = "protein-ontology-fragments.tsv"

  /** Filename of the static protein family file. */
  val StaticProteinFamilyFilename = "PFAM-families.tsv"

  /** Filename of the secondary static protein family file. */
  val StaticProteinFamily2Filename = "ProteinFamilies.tsv"

  /** Filename of the static protein family or complex file. */
  val StaticProteinFamilyOrComplexFilename = "famplex.tsv"

  /** Filename of the static disease file. */
  val StaticDiseaseFilename = "mesh-disease.tsv"

  /** Filename of the context species file */
  val ContextSpeciesFilename = "Species.tsv"

  /** Filename of the context cell lines file */
  val ContextCellLineFilename = "Cellosaurus.tsv"

  /** Filename of the secondary context cell lines file */
  val ContextCellLine2Filename = "atcc.tsv"

  /** Filename of the context cell types file */
  val ContextCellTypeFilename = "CellOntology.tsv"

  /** Filename of the context organs file */
  val ContextOrganFilename = "Uberon.tsv"

  /** Filename of the static tissue type file. */
  val ContextTissueTypeFilename = "tissue-type.tsv"


  /** Filename of a file containing just Gene Name Affix strings, extracted from
      the Sorger bioentities file. */
  val GeneNameAffixesFilename = "geneNameAffixes.txt"

  /** Filename of the protein kinases lookup table. */
  val ProteinKinasesFilename = "uniprot-kinases.txt"

  /** Filename of a file containing just Protein Domain suffixes; one per line. */
  val ProteinDomainShortNamesFilename = "proteinDomains-short.txt"

}
