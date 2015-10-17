package net.binangkit.payment.biller.pelangi.dummy

import scalaz.concurrent.Task
import org.http4s.UrlForm
import org.http4s.dsl.{/, ->, POST, BadRequest, BadRequestSyntax, Ok, OkSyntax, Root}
import org.http4s.server.HttpService
import org.http4s.Response
import org.http4s.dsl.{Ok, OkSyntax}

object Dummy {
  val preInqResp = """{"data":{"trx":{"trx_id":"","stan":"000000040991","datetime":"20151013160414","merchant_code":"6021","bank_code":"4510017","rc":"0000","terminal_id":"0000000000001374","material_number":"14234567891","subscriber_id":"551111111111","pln_refno":"A1D60ECDB792D1711FDE78CC50319681","switcher_refno":"19D06AEE853EF040991F94803BAB40B8","subscriber_name":"BUDIM\"AN\/.,STEAHWAN HD   ","subscriber_segmentation":"R1  ","power":654321,"admin_charge":1600,"distribution_code":"51","service_unit":"51106","service_unit_phone":"900            ","max_kwh_unit":"06000","total_repeat":"2","power_purchase_unsold":"1000000","power_purchase_unsold2":"","saldo":"","bit11":"","bit12":"","bit48":"JTL53L3142345678915511111111110A1D60ECDB792D1711FDE78CC5031968119D06AEE853EF040991F94803BAB40B8BUDIM\"AN\/.,STEAHWAN HD   R1  00065432120000000000","bit62":"5151106900            0600020000100000000010000000"}}}"""

  val prePayResp = """{"data":{"trx":{"trx_id":"525","stan":"40992","harga":"50400.0","saldo":"4178757","datetime":"20151013160428","merchant_code":"6021","bank_code":"4510017","rc":"0000","terminal_id":"0000000000001374","material_number":"14234567891","subscriber_id":"551111111111","pln_refno":"749D5E4F7CDF50409929A9763F9C544D","switcher_refno":"0BSM210Z2C63324D0D9787423E7D5F3D","subscriber_name":"BUDIM\"AN\/.,STEAHWAN HD   ","subscriber_segmentation":"R1  ","power":654321,"admin_charge":1600,"distribution_code":"51","service_unit":"51106","service_unit_phone":"900            ","max_kwh_unit":"","total_repeat":"2","token":"14234567891234040992","amount":51600,"angsuran":"3409.09","power_purchase":"37500.00","jml_kwh":"40.52","ppn":"5000.00","ppj":"4090.91","meterai":"0.00","power_purchase_unsold":"null","power_purchase_unsold2":"00010000000","bit11":"","bit12":"","bit48":"JTL53L3142345678915511111111110749D5E4F7CDF50409929A9763F9C544D0BSM210Z2C63324D0D9787423E7D5F3D00112233BUDIM\"AN\/.,STEAHWAN HD   R1  0006543210200000000002000000000020000500000200004090912000034090920000037500002000000405214234567891234040992","bit62":"5151106900            0600020000100000000010000000","info_text":"Informasi Hubungi Call Center 123 Atau hubungi PLN Terdekat"}}}"""

  val service = HttpService {
    case req@POST -> Root => {
      req.decode[UrlForm]{data =>
        data.getFirst("product_id") match {
          case Some("80") => data.getFirst("trx_type") match {
            case Some("2100") => Ok(preInqResp)
            case Some("2200") => Ok(prePayResp)
            case _ => BadRequest("Invalid trx_type")
          }
          case Some("100") => data.getFirst("trx_type") match {
            case Some("2100") => Ok(postInqResp)
            case _ => BadRequest("Invalid trx_type")
          }
          case _ => BadRequest("Invalid product_id")
        }
      }
    }
  }

  val postInqResp = """{"data":{"trx":{"bill_status":"4","bills":[{"previous_meter_reading1":"02362500","incentive":"D0000000000","meter_read_date":"00000000","bill_period":"201409","total_electricity_bill":"00000114620","value_added_tax":"0000000000","current_meter_reading3":"00000000","produk":"PLNPOSTPAID","previous_meter_reading3":"00000000","current_meter_reading2":"00000000","penalty_fee":"000009000","current_meter_reading1":"02381400","due_date":"20092014","previous_meter_reading2":"00000000"},{"previous_meter_reading1":"02381400","incentive":"D0000000000","meter_read_date":"00000000","bill_period":"201410","total_electricity_bill":"00000110840","value_added_tax":"0000000000","current_meter_reading3":"00000000","produk":"PLNPOSTPAID","previous_meter_reading3":"00000000","current_meter_reading2":"00000000","penalty_fee":"000006000","current_meter_reading1":"02399600","due_date":"20102014","previous_meter_reading2":"00000000"},{"previous_meter_reading1":"02399600","incentive":"D0000000000","meter_read_date":"00000000","bill_period":"201411","total_electricity_bill":"00000099510","value_added_tax":"0000000000","current_meter_reading3":"00000000","produk":"PLNPOSTPAID","previous_meter_reading3":"00000000","current_meter_reading2":"00000000","penalty_fee":"000003000","current_meter_reading1":"02415700","due_date":"20112014","previous_meter_reading2":"00000000"},{"previous_meter_reading1":"02415700","incentive":"D0000000000","meter_read_date":"00000000","bill_period":"201412","total_electricity_bill":"00000080000","value_added_tax":"0000000000","current_meter_reading3":"00000000","produk":"PLNPOSTPAID","previous_meter_reading3":"00000000","current_meter_reading2":"00000000","penalty_fee":"000003000","current_meter_reading1":"02420000","due_date":"20122014","previous_meter_reading2":"00000000"}],"trx_id":"","datetime":"20151017154402","subscriber_id":"211001025251","subscriber_name":"DEVEL POSTPAID 5BLN TRX1 ","amount":"432370","stand_meter_summary":"02362500 - 02420000","subscriber_segmentation":"  R1","switcher_refno":"172E121B75E029AF617D142001632A2B","stan":"000000041532","terminal_id":"0000000000001374","blth_summary":"SEP14, OKT14, NOV14, DES14","admin_charge":"6400","merchant_code":"6021","outstanding_bill":"5","material_number":"","power":"900","bank_code":"4510017","rc":"0000"}}}"""
}