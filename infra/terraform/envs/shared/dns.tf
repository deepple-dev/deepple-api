# 도메인 등록 업체에 이 영역의 네임서버가 등록돼 있어 새로 만들지 않고 가져온다
import {
  to = aws_route53_zone.this
  id = "Z04617637MTSJEOXOHO1"
}

resource "aws_route53_zone" "this" {
  name    = "deepple.co.kr"
  comment = "deepple.co.kr"

  lifecycle {
    prevent_destroy = true
  }
}

import {
  to = aws_route53_record.root_a
  id = "Z04617637MTSJEOXOHO1_deepple.co.kr_A"
}

resource "aws_route53_record" "root_a" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "deepple.co.kr"
  type    = "A"
  ttl     = 300
  records = ["216.198.79.1"]
}

import {
  to = aws_route53_record.www
  id = "Z04617637MTSJEOXOHO1_www.deepple.co.kr_CNAME"
}

resource "aws_route53_record" "www" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "www.deepple.co.kr"
  type    = "CNAME"
  ttl     = 300
  records = ["cname.vercel-dns.com."]
}

import {
  to = aws_route53_record.mx
  id = "Z04617637MTSJEOXOHO1_deepple.co.kr_MX"
}

resource "aws_route53_record" "mx" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "deepple.co.kr"
  type    = "MX"
  ttl     = 3600
  records = [
    "10 kr1-aspmx1.worksmobile.com",
    "20 kr1-aspmx2.worksmobile.com",
  ]
}

import {
  to = aws_route53_record.txt
  id = "Z04617637MTSJEOXOHO1_deepple.co.kr_TXT"
}

resource "aws_route53_record" "txt" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "deepple.co.kr"
  type    = "TXT"
  ttl     = 3600
  records = [
    "v=spf1 include:spf.worksmobile.com ~all",
    "google-site-verification=L35Q3iJiMLQeGpwBxV1EiWpNUdFkBv8GFDzIubMDNlQ",
  ]
}

import {
  to = aws_route53_record.admin
  id = "Z04617637MTSJEOXOHO1_admin.deepple.co.kr_A"
}

resource "aws_route53_record" "admin" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "admin.deepple.co.kr"
  type    = "A"

  alias {
    name                   = "d10xiw3yu57gj.cloudfront.net"
    zone_id                = "Z2FDTNDATAQYW2" # CloudFront 고정 영역 ID
    evaluate_target_health = false
  }
}

# admin.deepple.co.kr ACM 인증서 검증 레코드
import {
  to = aws_route53_record.admin_cert_validation
  id = "Z04617637MTSJEOXOHO1__982d85a5162922e17d5d151ee7d7fafd.admin.deepple.co.kr_CNAME"
}

resource "aws_route53_record" "admin_cert_validation" {
  zone_id = aws_route53_zone.this.zone_id
  name    = "_982d85a5162922e17d5d151ee7d7fafd.admin.deepple.co.kr"
  type    = "CNAME"
  ttl     = 300
  records = ["_9856fab2a1f333a384e6e367b33756a1.jkddzztszm.acm-validations.aws."]
}
