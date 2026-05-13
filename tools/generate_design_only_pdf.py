from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs" / "design-only-improvements.md"
OUTPUT = ROOT / "ashokvatika_design_only_improvements.pdf"


def build_styles():
    styles = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "Title",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=20,
            leading=24,
            textColor=colors.HexColor("#1B3A2F"),
            alignment=TA_LEFT,
            spaceAfter=10,
        ),
        "heading": ParagraphStyle(
            "Heading",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=13,
            leading=17,
            textColor=colors.HexColor("#D96B1F"),
            spaceBefore=8,
            spaceAfter=4,
        ),
        "body": ParagraphStyle(
            "Body",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=10.5,
            leading=15,
            textColor=colors.HexColor("#222222"),
            spaceAfter=4,
        ),
        "bullet": ParagraphStyle(
            "Bullet",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=10.5,
            leading=15,
            leftIndent=14,
            firstLineIndent=-10,
            bulletIndent=4,
            textColor=colors.HexColor("#222222"),
            spaceAfter=2,
        ),
    }


def parse_markdown(lines):
    blocks = []
    paragraph = []

    def flush_paragraph():
        nonlocal paragraph
        if paragraph:
            blocks.append(("p", " ".join(paragraph).strip()))
            paragraph = []

    for raw in lines:
        line = raw.rstrip()
        if not line.strip():
            flush_paragraph()
            continue
        if line.startswith("# "):
            flush_paragraph()
            blocks.append(("title", line[2:].strip()))
            continue
        if line.startswith("## ") or line.startswith("### "):
            flush_paragraph()
            blocks.append(("h", line.split(" ", 1)[1].strip()))
            continue
        if line.startswith("- "):
            flush_paragraph()
            blocks.append(("b", line[2:].strip()))
            continue
        if len(line) >= 4 and line[:2].isdigit() and line[2:4] == ". ":
            flush_paragraph()
            blocks.append(("b", line.strip()))
            continue
        paragraph.append(line.strip())

    flush_paragraph()
    return blocks


def escape(text: str) -> str:
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def main():
    styles = build_styles()
    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=18 * mm,
        rightMargin=18 * mm,
        topMargin=16 * mm,
        bottomMargin=16 * mm,
        title="Ashokvatika Design Improvement Report",
        author="OpenAI Codex",
    )

    story = []
    blocks = parse_markdown(SOURCE.read_text(encoding="utf-8").splitlines())
    for block_type, text in blocks:
        safe = escape(text)
        if block_type == "title":
            story.append(Paragraph(safe, styles["title"]))
            story.append(Spacer(1, 2))
        elif block_type == "h":
            story.append(Paragraph(safe, styles["heading"]))
        elif block_type == "b":
            story.append(Paragraph(safe, styles["bullet"], bulletText="-"))
        else:
            story.append(Paragraph(safe, styles["body"]))

    doc.build(story)
    print(OUTPUT)


if __name__ == "__main__":
    main()
